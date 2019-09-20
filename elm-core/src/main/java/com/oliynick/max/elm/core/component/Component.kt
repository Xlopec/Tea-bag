/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package com.oliynick.max.elm.core.component

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * Default capacity of actor's _processing mailbox_
 */
private const val DEFAULT_ACTOR_BUFFER_CAPACITY = 1U

/**
 * An alias for a pure function that accepts message with current state and returns the next state with possible empty set of commands
 * to feed [resolver][Resolver]
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 */
typealias Update<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>

/**
 * An alias for function to resolve effects and return messages to feed [update][Update] function
 * @param M incoming messages
 * @param C commands to be executed
 */
typealias Resolver<C, M> = suspend (command: C) -> Set<M>

/**
 * An alias for result of [update][Update] function
 * @param S state of the component
 * @param C commands to be executed
 */
typealias UpdateWith<S, C> = Pair<S, Set<C>>

/**
 * An alias for function that accepts input flow of messages and returns flow of states produced by that messages
 * @param M incoming messages
 * @param S state of the component
 */
typealias Component<M, S> = (Flow<M>) -> Flow<S>

typealias Loader<S> = suspend () -> S

/**
 * Component is one of the main parts of the [ELM architecture](https://guide.elm-lang.org/architecture/). Component (Runtime)
 * is a stateful part of the application responsible for a specific feature.
 *
 * Conceptually component is a triple [message][M], [command][C], [state][S] operated by pure [update][Update] and impure [resolver][Resolver]
 * functions. Each component accepts flow of [messages][M] and produces flow of [states][S] triggered by that messages.
 * Components can be bound to each other to produce new, more complex components
 *
 * Note that the resulting function always returns the last state value to its subscribers
 *
 * Component's behaviour can be configured by passing corresponding implementations of [resolver] and [update] functions
 *
 * @receiver scope where the component should be placed
 * @param loader loader to retrieve initial state of the component
 * @param resolver function to resolve effects
 * @param update pure function to compute states and effects to be resolved
 * @param initialCommand initial command to execute
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 */
fun <M, C, S> CoroutineScope.component(loader: Loader<S>,
                                       resolver: Resolver<C, M>,
                                       update: Update<M, S, C>,
                                       initialCommand: C? = null): Component<M, S> {

    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    return newComponent(statesChannel.asFlow(),
                        newActor(loader, resolver, update, initialCommand, coroutineContext.jobOrDefault(), statesChannel))
}

/**
 * Component is one of the main parts of the [ELM architecture](https://guide.elm-lang.org/architecture/). Component (Runtime)
 * is a stateful part of the application responsible for a specific feature.
 *
 * Conceptually component is a triple [message][M], [command][C], [state][S] operated by pure [update][Update] and impure [resolver][Resolver]
 * functions. Each component accepts flow of [messages][M] and produces flow of [states][S] triggered by that messages.
 * Components can be bound to each other to produce new, more complex components
 *
 * Note that the resulting function always returns the last state value to its subscribers
 *
 * Component's behaviour can be configured by passing corresponding implementations of [resolver] and [update] functions
 *
 * @receiver scope where the component should be placed
 * @param initialState initial state of the component
 * @param resolver function to resolve effects
 * @param update pure function to compute states and effects to be resolved
 * @param initialCommand initial command to execute
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 */
fun <M, C, S> CoroutineScope.component(initialState: S,
                                       resolver: Resolver<C, M>,
                                       update: Update<M, S, C>,
                                       initialCommand: C? = null): Component<M, S> {

    @Suppress("RedundantSuspendModifier")
    suspend fun loader(): S = initialState

    return component(::loader, resolver, update, initialCommand)
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

operator fun <M, S> Component<M, S>.invoke(vararg messages: M) = this(flowOf(*messages))

operator fun <M, S> Component<M, S>.invoke(message: M) = this(flowOf(message))

/**
 * Takes changes of the [producer] stream and feeds them as input to the [consumer] applying [transform] function
 *
 * @receiver coroutine scope to be used
 * @param producer the producer flow function
 * @param consumer the consumer flow function
 * @param transform function that maps produced states to the flow to be consumed by the consumer function
 */
inline fun <M1, S1, M2, S2> CoroutineScope.bind(noinline producer: Component<M1, S1>,
                                                noinline consumer: Component<M2, S2>,
                                                crossinline transform: (S1) -> Flow<M2>) {

    launch { producer.changes().collect { s1 -> launch { consumer(transform(s1)).collect() } } }
}

private suspend fun <E> SendChannel<E>.sendChecking(e: E) {
    check(!isClosedForSend) { "Component was already disposed" }
    send(e)
}

private suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Set<C>): Set<M> {
    return commands.fold(HashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }
}

private suspend fun <V> Channel<V>.send(values: Iterable<V>) = values.forEach { v -> send(v) }

/**
 * Retrieves latest element from this [conflated channel][BroadcastChannel]
 *
 * @throws IllegalArgumentException if receiver channel isn't [conflated][Channel.CONFLATED]
 */
private inline val <S> BroadcastChannel<S>.latest: S
    get() = requireNotNull(openSubscription().poll()!!) { "What a terrible failure!" }

private fun CoroutineContext.jobOrDefault(): Job = this[Job.Key] ?: Job()

private fun <M, C, S> CoroutineScope.newActor(loader: Loader<S>,
                                              resolver: Resolver<C, M>,
                                              update: Update<M, S, C>,
                                              initialCommand: C?,
                                              parentJob: Job,
                                              statesChannel: BroadcastChannel<S>): SendChannel<M> {

    @UseExperimental(ObsoleteCoroutinesApi::class)
    return this@newActor.actor(parentJob, DEFAULT_ACTOR_BUFFER_CAPACITY.toInt(), onCompletion = statesChannel::close) {
        check(statesChannel.offer(loader())) { "Couldn't offer an initial state" }

        if (initialCommand != null) {
            channel.send(resolver(initialCommand))
        }

        for (message in channel) {
            val (nextState, commands) = update(message, statesChannel.latest)
            // we don't want to get blocked here
            check(statesChannel.offer(nextState)) { "Couldn't offer next state $nextState" }
            channel.send(resolver(commands))
        }
    }
}

/**
 * Combines given flow of states and message channel into TEA component
 */
private fun <M, S> newComponent(state: Flow<S>, messages: SendChannel<M>): Component<M, S> {
    return { input ->

        channelFlow {

            launch {
                state.distinctUntilChanged().collect {
                    send(it)
                }
            }

            launch {
                input.collect {
                    messages.sendChecking(it)
                }
            }
        }
    }
}

private fun <S> BroadcastChannel(initial: S) = BroadcastChannel<S>(Channel.CONFLATED)
    .also { channel -> channel.offer(initial) }