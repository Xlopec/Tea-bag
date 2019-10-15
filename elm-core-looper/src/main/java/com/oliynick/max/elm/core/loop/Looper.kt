/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.elm.core.loop

import com.oliynick.max.elm.core.component.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Internal alias of a component
 */
typealias ComponentInternal<M, S> = Pair<SendChannel<M>, Flow<S>>

/**
 * Dependencies holder
 */
data class Dependencies<M, C, S>(inline val initializer: Initializer<S, C>,
                                          inline val resolver: Resolver<C, M>,
                                          inline val update: Update<M, S, C>,
                                          inline val interceptor: Interceptor<M, S, C>)

/**
 * Stores a new state to channel and notifies subscribers about changes
 */
suspend fun <M, C, S> updateMutating(message: M,
                                     state: S,
                                     dependencies: Dependencies<M, C, S>,
                                     states: BroadcastChannel<S>): UpdateWith<S, C> {

    return dependencies.update(message, state)
        // we don't want to suspend here
        .also { (nextState, _) -> states.offerChecking(nextState) }
        .also { (nextState, commands) -> dependencies.interceptor(message, state, nextState, commands) }
}

/**
 * Polls messages from channel's iterator and computes subsequent component's states.
 * Before polling a message from the channel it tries to computes all
 * subsequent states produced by resolved commands
 */
tailrec suspend fun <M, C, S> loop(state: S,
                                   it: ChannelIterator<M>,
                                   dependencies: Dependencies<M, C, S>,
                                   states: BroadcastChannel<S>): S {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = updateMutating(message, state, dependencies, states)

    return loop(loop(nextState, dependencies.resolver(commands).iterator(), dependencies, states), it, dependencies, states)
}

/**
 * Polls messages from collection's iterator and computes next states until collection is empty
 */
suspend fun <M, C, S> loop(state: S,
                           it: Iterator<M>,
                           dependencies: Dependencies<M, C, S>,
                           states: BroadcastChannel<S>): S {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = updateMutating(message, state, dependencies, states)

    return loop(loop(nextState, it, dependencies, states), dependencies.resolver(commands).iterator(), dependencies, states)
}

/**
 * Loads an initial state using supplied initializer and starts component's loop
 */
suspend fun <M, C, S> loop(initializer: Initializer<S, C>,
                           dependencies: Dependencies<M, C, S>,
                           messages: Channel<M>,
                           states: BroadcastChannel<S>): S {

    val (initState, initCommands) = initializer()
        .also { (initialState, _) -> states.offerChecking(initialState) }

    val nonTransient = loop(initState, dependencies.resolver(initCommands).iterator(), dependencies, states)

    return loop(nonTransient, messages.iterator(), dependencies, states)
}

/**
 * Combines given flow of states and message channel into TEA component
 */
fun <M, S> newComponent(state: Flow<S>, messages: SendChannel<M>): Component<M, S> {
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
suspend fun emptyInterceptor(message: Any, prevState: Any, newState: Any, commands: Set<*>) = Unit

fun <E> BroadcastChannel<E>.offerChecking(e: E) = check(offer(e)) { "Couldn't offer next element - $e" }

fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

suspend fun <E> ChannelIterator<E>.nextOrNull() = if (hasNext()) next() else null

suspend fun <E> SendChannel<E>.sendChecking(e: E) {
    check(!isClosedForSend) { "Component was already disposed" }
    send(e)
}

suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> {
    return commands.fold(HashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }
}

val Unit?.safe get() = this