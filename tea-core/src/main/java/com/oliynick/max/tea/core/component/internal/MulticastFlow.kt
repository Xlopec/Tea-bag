package com.oliynick.max.tea.core.component.internal

import com.oliynick.max.tea.core.UnstableApi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.ValueOrClosed
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@UnstableApi
fun <T> Flow<T>.shareConflated(
    scope: CoroutineScope = GlobalScope,
): Flow<T> =
    MulticastFlow(this, true, scope).multicastedFlow

/**
 * Allow multiple collectors to collect same instance of this flow
 *
 * before closing original flow. Set to 0 to disable.
 */
@UnstableApi
fun <T> Flow<T>.share(
    scope: CoroutineScope = GlobalScope,
): Flow<T> = MulticastFlow(this, false, scope).multicastedFlow

/**
 * https://gist.github.com/matejdro/a9c838bf0066595fb52b4b8816f49252
 */
@OptIn(InternalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
private class MulticastFlow<T>(
    private val original: Flow<T>,
    private val conflate: Boolean,
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val collectors = ArrayList<SendChannel<T>>()

    private var lastValue: Result<T>? = null

    private var actor = Channel<MulticastActorAction<T>>(Channel.BUFFERED)
    private var flowChannel: ReceiveChannel<T>? = null

    private var multicastActorJob: Job? = null

    private suspend fun ensureActorActive() {
        mutex.withLock {
            if (multicastActorJob?.isActive != true) {
                startFlowActor()
            }
        }
    }

    private fun startFlowActor() {
        // Create new channel to clear buffer of the previous channel
        actor = Channel(Channel.BUFFERED)
        multicastActorJob = scope.launch {
            while (isActive) {
                val currentFlowChannel = flowChannel

                select<Unit> {
                    actor.onReceive { action ->
                        onActorAction(action)
                    }

                    @Suppress("IfThenToSafeAccess")
                    if (currentFlowChannel != null) {
                        currentFlowChannel.onReceiveOrClosed { valueOrClosed ->
                            onOriginalFlowData(valueOrClosed)
                        }
                    }
                }
            }
        }
    }

    private suspend fun onActorAction(action: MulticastActorAction<T>) {
        mutex.withLock {
            when (action) {
                is MulticastActorAction.AddCollector -> {
                    collectors.add(action.channel)

                    if (flowChannel == null) {
                        flowChannel = original.produceIn(scope)
                    }

                    val lastValue = lastValue
                    if (lastValue != null) {
                        action.channel.send(lastValue.getOrThrow())
                    }
                }
                is MulticastActorAction.RemoveCollector -> {

                    val collectorIndex = collectors.indexOf(action.channel)

                    if (collectorIndex >= 0) {
                        val removedCollector = collectors.removeAt(collectorIndex)
                        removedCollector.close()
                    }

                    if (collectors.isEmpty()) {
                        closeOriginalFlow()
                    }
                }
            }
        }
    }

    private suspend fun closeOriginalFlow() {
        lastValue = null
        flowChannel?.cancel()
        flowChannel = null
        multicastActorJob?.cancelAndJoin()
    }

    private suspend fun onOriginalFlowData(valueOrClosed: ValueOrClosed<T>) {
        if (valueOrClosed.isClosed) {
            mutex.withLock {
                collectors.forEach { it.close(valueOrClosed.closeCause) }
                collectors.clear()
                closeOriginalFlow()
            }
        } else {
            collectors.forEach {
                try {
                    if (conflate) {
                        lastValue = Result.success(valueOrClosed.value)
                    }
                    it.send(valueOrClosed.value)
                } catch (e: Exception) {
                    // Ignore downstream exceptions
                }
            }
        }
    }

    val multicastedFlow = flow {
        val channel = Channel<T>()

        try {
            ensureActorActive()
            actor.send(
                MulticastActorAction.AddCollector(
                    channel
                )
            )

            emitAll(channel.consumeAsFlow())
        } catch (th: Throwable) {
            throw th
        } finally {
            actor.send(
                MulticastActorAction.RemoveCollector(
                    channel
                )
            )
        }
    }

    private sealed class MulticastActorAction<T> {
        class AddCollector<T>(val channel: SendChannel<T>) : MulticastActorAction<T>()
        class RemoveCollector<T>(val channel: SendChannel<T>) : MulticastActorAction<T>()
    }
}
