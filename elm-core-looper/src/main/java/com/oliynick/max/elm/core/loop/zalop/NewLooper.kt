package com.oliynick.max.elm.core.loop.zalop

import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.Regular
import com.oliynick.max.elm.core.component.Snapshot
import com.oliynick.max.elm.core.loop.invoke
import com.oliynick.max.elm.core.loop.nextOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/**
 * Polls messages from channel's iterator and computes subsequent component's states.
 * Before polling a message from the channel it tries to computes all
 * subsequent states produced by resolved commands
 */
suspend fun <M, C, S> Env<M, C, S>.loopNew(
    state: Snapshot<M, S, C>,
    it: ChannelIterator<M>,
    states: Channel<Snapshot<M, S, C>>
): Snapshot<M, S, C> {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = update(message, state.state)

    val snapshot = Regular(message, nextState, commands)

    states.send(snapshot)

    return loopNew(
        loopNew(
            snapshot,
            resolver(commands).iterator(),
            states
        ),
        it,
        states
    )
}

/**
 * Polls messages from collection's iterator and computes next states until collection is empty
 */
suspend fun <M, C, S> Env<M, C, S>.loopNew(
    state: Snapshot<M, S, C>,
    it: Iterator<M>,
    states: Channel<Snapshot<M, S, C>>
): Snapshot<M, S, C> {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = update(message, state.state)

    val snapshot = Regular(message, nextState, commands)

    states.send(snapshot)

    return loopNew(
        loopNew(
            snapshot,
            it,
            states
        ),
        resolver(commands).iterator(),
        states
    )
}

/**
 * Loads an initial state using supplied initializer and starts component's loop
 */
suspend fun <M, C, S> Env<M, C, S>.loopNew(
    messages: Channel<M>,
    states: Channel<Snapshot<M, S, C>>
): Snapshot<M, S, C> {

    val initial = newInitializer()

    states.send(initial)

    val nonTransient = loopNew(initial, resolver(initial.commands).iterator(), states)

    return loopNew(nonTransient, messages.iterator(), states)
}

fun <M, C, S> CoroutineScope.Component(
    env: Env<M, C, S>
): Component<M, S, C> {

    val snapshots = Channel<Snapshot<M, S, C>>(Channel.RENDEZVOUS)
    val shared = snapshots.broadcast()
    val state = AtomicReference<Snapshot<M, S, C>>()

    @UseExperimental(ObsoleteCoroutinesApi::class)
    val input = actor<M>(
        coroutineContext.jobOrDefault(),
        start = CoroutineStart.LAZY,
        onCompletion = snapshots::close
    ) {

        env.loopNew(channel, snapshots)

    }

    return { messages ->

        channelFlow {

            launch {
                messages.collect { message ->
                    input.send(message)
                }
            }


            shared.asFlow()
                .collect { state ->
                println("send $state")

                    send(state)

                  //  shared.offer(state)
            }
        }
    }
}

private fun CoroutineContext.jobOrDefault(): Job = this[Job] ?: Job()


private inline fun <T, R> Flow<T>.scanFlatten(
    acc: R,
    crossinline transform: suspend (R, T) -> Flow<R>
): Flow<R> {

    var current = acc

    return flatMapMerge { next ->
        transform(current, next)
            .onEach { new -> current = new }
    }.startWith(acc)
}

private fun <T> Flow<T>.startWith(
    t: T
) = onStart { emit(t) }
