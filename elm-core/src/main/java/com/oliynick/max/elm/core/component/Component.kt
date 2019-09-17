@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package com.oliynick.max.elm.core.component

import com.oliynick.max.elm.core.misc.mergeWith
import com.oliynick.max.elm.core.misc.startWith
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

typealias Update<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>
typealias Resolver<C, M> = suspend (command: C) -> Set<M>
typealias UpdateWith<S, C> = Pair<S, Set<C>>
typealias Component<M, S> = (Flow<M>) -> Flow<S>

/**
 * Component is one of the main parts of the [ELM architecture](https://guide.elm-lang.org/architecture/). Component (Runtime)
 * is a stateful part of the application responsible for a specific feature.
 *
 * Conceptually component is a triple [message][M], [command][C], [state][S] operated by pure [update][Update] and impure [resolver][Resolver]
 * functions. Each component accepts flow of [messages][M] and produces flow of [states][S] triggered by that messages.
 * Components can be bound to each other to produce new, more complex components
 *
 * Note that the resulting function always returns the last value to its subscribers
 *
 * Component's behaviour can be configured by passing corresponding implementations of [resolver] and [update] functions
 */
fun <M, C, S> component(initialState: S, resolver: Resolver<C, M>, update: Update<M, S, C>): Component<M, S> {
    return ComponentImpl(initialState, resolver, update)
}

private class ComponentImpl<M, C, S>(initialState: S,
                                     private val resolver: Resolver<C, M>,
                                     private val update: Update<M, S, C>) : (Flow<M>) -> Flow<S> {

    private val stateChannel = BroadcastChannel<S>(Channel.CONFLATED)
        .also { channel -> channel.offer(initialState) }

    override fun invoke(messages: Flow<M>): Flow<S> {
        // merge with channel since changes made to state via another calls to this method
        // should be propagated to all subscribers. For example, it may happen when both
        // another component and the UI one are subscribed to this component
        return stateChannel.asFlow().distinctUntilChanged().mergeWith(calculateState(messages))
    }

    private fun calculateState(messages: Flow<M>): Flow<S> {
        return messages.map { message -> update(message, stateChannel.latest()) }
            .flatMapConcat { (nextState, commands) ->
                // do recursive calls to compute a stable state
                // TODO should I replace it with a loop?
                commands.map { cmd -> calculateState(resolver(cmd).asFlow()) }
                    .asFlow()
                    .flattenConcat()
                    .startWith(nextState)
            }
            // save and notify subscribers about new state
            .onEach { newState -> stateChannel.offer(newState) }
            // avoid duplicates
            .distinctUntilChanged()
    }

}

infix fun <S, C> S.command(command: C) = this to setOf(command)

fun <S, C> S.command(first: C, second: C) = this to setOf(first, second)

fun <S, C> S.command(first: C, second: C, third: C) = this to setOf(first, second, third)

fun <S, C> S.command(vararg commands: C) = this to setOf(*commands)

fun <S, C> S.noCommand() = this to emptySet<C>()

/**
 * Handy wrapper to perform side effect computations within coroutine scope. This function always
 * returns empty set of messages [M]
 */
suspend inline fun <C, M> C.sideEffect(crossinline action: suspend C.() -> Unit): Set<M> {
    return coroutineScope { action(); emptySet() }
}

/**
 * Handy wrapper to perform side effect computations within coroutine scope
 */
suspend inline fun <C, M> C.effect(crossinline action: suspend C.() -> M?): Set<M> {
    return coroutineScope { action(this@effect)?.let(::setOf) ?: emptySet() }
}

/**
 * Returns flow that listens to changes of the original function
 */
fun <M, S> ((Flow<M>) -> Flow<S>).changes(): Flow<S> = this(emptyFlow())

/**
 * Takes changes of the [producer] stream and feeds them as input to the [consumer] applying [transform] function
 *
 * @param producer the producer flow function
 * @param consumer the consumer flow function
 * @param scope coroutine scope to be used
 * @param transform function that maps produced states to the flow to be consumed by the consumer function
 */
inline fun <M1, S1, M2, S2> bind(noinline producer: (Flow<M1>) -> Flow<S1>,
                                 noinline consumer: (Flow<M2>) -> Flow<S2>,
                                 scope: CoroutineScope = GlobalScope,
                                 crossinline transform: (S1) -> Flow<M2>): Job {

    return with(scope) { launch { producer.changes().collect { s1 -> launch { consumer(transform(s1)).collect() } } } }
}

private fun <S> BroadcastChannel<S>.latest() = checkNotNull(openSubscription().poll()!!) { "What a terrible failure!" }