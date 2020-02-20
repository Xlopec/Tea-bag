package com.oliynick.max.tea.core.component.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.ValueOrClosed
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <T> Flow<T>.shareConflated(
    debounceMs: Int = 0
): Flow<T> =
    MulticastFlow(this, true, debounceMs).multicastedFlow

/**
 * Allow multiple collectors to collect same instance of this flow
 *
 * @param debounceMs Number of milliseconds to wait after last collector closes
 * before closing original flow. Set to 0 to disable.
 */
fun <T> Flow<T>.share(
    debounceMs: Int = 0
): Flow<T> = MulticastFlow(
    this,
    false,
    debounceMs
).multicastedFlow

/**
 * https://gist.github.com/matejdro/a9c838bf0066595fb52b4b8816f49252
 */
@UseExperimental(InternalCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
private class MulticastFlow<T>(
    private val original: Flow<T>,
    private val conflate: Boolean,
    private val debounceMs: Int
) {
    private val mutex = Mutex()
    private val collectors = ArrayList<SendChannel<T>>()

    private var lastValue: Result<T>? = null
    private var nextDebounceTarget: Long = -1L

    private var actor = Channel<MulticastActorAction<T>>(Channel.BUFFERED)
    private var flowChannel: ReceiveChannel<T>? = null

    private var multicastActorJob: Job? = null

    private suspend fun ensureActorActive() {
        if (multicastActorJob?.isActive != true) {
            mutex.withLock {
                if (multicastActorJob?.isActive != true) {
                    startFlowActor()
                }
            }
        }
    }

    private fun startFlowActor() {
        // Create new channel to clear buffer of the previous channel
        actor = Channel(Channel.BUFFERED)
        multicastActorJob = GlobalScope.launch {
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

                    if (nextDebounceTarget >= 0) {
                        onTimeout(nextDebounceTarget - System.currentTimeMillis()) {
                            closeOriginalFlow()
                            nextDebounceTarget = -1
                        }
                    }
                }
            }
        }
    }

    private suspend fun onActorAction(action: MulticastActorAction<T>) {
        when (action) {
            is MulticastActorAction.AddCollector -> {
                collectors.add(action.channel)

                if (flowChannel == null) {
                    flowChannel = original.produceIn(GlobalScope)
                }

                val lastValue = lastValue
                if (lastValue != null) {
                    action.channel.send(lastValue.getOrThrow())
                }

                nextDebounceTarget = -1
            }
            is MulticastActorAction.RemoveCollector -> {
                val collectorIndex = collectors.indexOf(action.channel)

                if (collectorIndex >= 0) {
                    val removedCollector = collectors.removeAt(collectorIndex)
                    removedCollector.close()
                }

                if (collectors.isEmpty()) {
                    if (debounceMs > 0) {
                        nextDebounceTarget = System.currentTimeMillis() + debounceMs
                    } else {
                        closeOriginalFlow()
                    }
                }
            }
        }
    }

    private fun closeOriginalFlow() {
        lastValue = null
        flowChannel?.cancel()
        flowChannel = null
        multicastActorJob?.cancel()
    }

    private suspend fun onOriginalFlowData(valueOrClosed: ValueOrClosed<T>) {
        if (valueOrClosed.isClosed) {
            collectors.forEach { it.close(valueOrClosed.closeCause) }
            collectors.clear()
            closeOriginalFlow()
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
        }
        finally {
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
