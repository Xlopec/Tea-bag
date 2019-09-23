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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Default capacity of actor's _processing mailbox_
 */
private const val DEFAULT_ACTOR_BUFFER_CAPACITY = 1U

/**
 * Alias for a pure function that accepts message with current state and returns the next state with possible empty set of commands
 * to feed [resolver][Resolver]
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 */
typealias Update<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>

/**
 * Alias for a function that resolves effects and returns messages to feed [update][Update] function
 * @param M incoming messages
 * @param C commands to be executed
 */
typealias Resolver<C, M> = suspend (command: C) -> Set<M>

/**
 * Alias for result of the [update][Update] function
 * @param S state of the component
 * @param C commands to be executed
 */
typealias UpdateWith<S, C> = Pair<S, Set<C>>

/**
 * Alias for a function that accepts input flow of messages and returns flow of states produced by that messages
 * @param M incoming messages
 * @param S state of the component
 */
typealias Component<M, S> = (messages: Flow<M>) -> Flow<S>

/**
 * Alias for a function that loads initial state of the component and initial set of commands
 * @param C initial commands to execute
 * @param S initial state of the component
 */
typealias Initializer<S, C> = suspend () -> InitArgs<S, C>

/**
 * Alias for result of the [init][Initializer] function
 * @param C initial commands to execute
 * @param S initial state of the component
 */
typealias InitArgs<S, C> = Pair<S, Set<C>>

/**
 * Alias for a function that observes changes made inside component
 * @param M incoming message
 * @param C commands to be executed
 * @param S state of the component
 */
typealias Interceptor<M, S, C> = suspend (message: M, prevState: S, newState: S, commands: Set<C>) -> Unit

/**
 * Internal alias of a component
 */
internal typealias ComponentInternal<M, S> = Pair<SendChannel<M>, Flow<S>>

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
 * @param initializer initializer to supply initial args to the component
 * @param resolver function to resolve effects
 * @param update pure function to compute states and effects to be resolved
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 * @return configured instance of [Component]
 */
fun <M : Any, C : Any, S : Any> CoroutineScope.component(initializer: Initializer<S, C>,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor): Component<M, S> {

    val (messages, states) = actorComponent(initializer, resolver, update, interceptor)

    return newComponent(states, messages)
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
 * @param initialCommands initial set of commands to execute
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 * @return configured instance of [Component]
 */
fun <M : Any, C : Any, S : Any> CoroutineScope.component(initialState: S,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor,
                                                         vararg initialCommands: C): Component<M, S> {

    @Suppress("RedundantSuspendModifier")
    suspend fun loader() = initialState to setOf(*initialCommands)

    return component(::loader, resolver, update, interceptor)
}

private fun <M, C, S> CoroutineScope.actorComponent(initializer: Initializer<S, C>,
                                                    resolver: Resolver<C, M>,
                                                    update: Update<M, S, C>,
                                                    interceptor: Interceptor<M, S, C>): ComponentInternal<M, S> {

    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    @UseExperimental(ObsoleteCoroutinesApi::class)
    return this@actorComponent.actor<M>(coroutineContext.jobOrDefault(),
                                        DEFAULT_ACTOR_BUFFER_CAPACITY.toInt(),
                                        onCompletion = statesChannel::close) {
        // stores a new state to channel and notifies subscribers about changes
        suspend fun updateMutating(message: M, state: S): UpdateWith<S, C> {
            return update(message, state)
                // we don't want to suspend here
                .also { (nextState, _) -> statesChannel.offerChecking(nextState) }
                .also { (nextState, commands) -> interceptor(message, state, nextState, commands) }
        }
        // polls messages from collection's iterator and
        // computes next states until collection is empty
        suspend fun compute(state: S, it: Iterator<M>): S {
            val message = it.nextOrNull() ?: return state

            val (nextState, commands) = updateMutating(message, state)

            return compute(compute(nextState, it), resolver(commands).iterator())
        }
        // polls messages from channel's iterator and computes subsequent component's states.
        // Before polling a message from the channel it tries to computes all
        // subsequent states produced by resolved commands
        tailrec suspend fun compute(state: S, it: ChannelIterator<M>): S {
            val message = it.nextOrNull() ?: return state

            val (nextState, commands) = updateMutating(message, state)

            return compute(compute(nextState, resolver(commands).iterator()), it)
        }

        compute(computeNonTransientState(initializer, resolver, update, statesChannel), channel.iterator())

    } to statesChannel.asFlow()
}

/**
 * Loads initial state and set of commands, after that computes the sequence of states
 * until non-transient one is found
 * */
private suspend fun <M, C, S> computeNonTransientState(initializer: Initializer<S, C>,
                                                       resolver: Resolver<C, M>,
                                                       update: Update<M, S, C>,
                                                       statesChannel: BroadcastChannel<S>): S {

    suspend fun compute(state: S, it: Iterator<M>): S {
        val message = it.nextOrNull() ?: return state

        val (nextState, commands) = update(message, state)
            // we don't want to suspend here
            .also { (nextState, _) -> statesChannel.offerChecking(nextState) }

        return compute(compute(nextState, it), resolver(commands).iterator())
    }

    val (initialState, initialCommands) = initializer()

    statesChannel.offerChecking(initialState)

    return compute(initialState, resolver(initialCommands).iterator())
}

/**
 * Combines given flow of states and message channel into TEA component
 */
private fun <M, S> newComponent(state: Flow<S>, messages: SendChannel<M>): Component<M, S> {
    return { input ->

        channelFlow {

            launch {
                state.distinctUntilChanged().collect { state ->
                    send(state)
                }
            }

            launch {
                input.collect { message ->
                    messages.sendChecking(message)
                }
            }
        }
    }
}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
private suspend fun emptyInterceptor(message: Any, prevState: Any, newState: Any, commands: Set<*>) = Unit

private fun <E> BroadcastChannel<E>.offerChecking(e: E) = check(offer(e)) { "Couldn't offer next element - $e" }

private fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

private suspend fun <E> ChannelIterator<E>.nextOrNull() = if (hasNext()) next() else null

private suspend fun <E> SendChannel<E>.sendChecking(e: E) {
    check(!isClosedForSend) { "Component was already disposed" }
    send(e)
}

private suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> {
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
