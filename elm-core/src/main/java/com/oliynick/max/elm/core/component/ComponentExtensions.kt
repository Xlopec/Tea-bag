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

package com.oliynick.max.elm.core.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Handy extension to combine a single command with state
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param command command to combine with state
 * @return [UpdateWith] instance with given state and set that consists from a single command
 */
infix fun <S : Any, C : Any> S.command(command: C): UpdateWith<S, C> = this to setOf(command)

/**
 * Handy extension to combine two commands with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S : Any, C : Any> S.command(first: C, second: C): UpdateWith<S, C> = this to setOf(first, second)

/**
 * Handy extension to combine three commands with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @param third the third command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S : Any, C : Any> S.command(first: C, second: C, third: C): UpdateWith<S, C> = this to setOf(first, second, third)

/**
 * Handy extension to combine multiple commands with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S : Any, C : Any> S.command(vararg commands: C): UpdateWith<S, C> = this to setOf(*commands)

/**
 * Handy extension to express absence of commands to execute combined with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @return [UpdateWith] instance with given state and empty set of commands
 */
fun <S : Any, C : Any> S.noCommand(): UpdateWith<S, C> = this to emptySet()

/**
 * Handy wrapper to perform side effect computations within coroutine scope. This function always
 * returns empty set of messages [M]
 * @param action action to perform that produces no messages that can be consumed by a component
 * @return set of messages to be consumed by a component, always empty
 */
suspend inline fun <C : Any, M : Any> C.sideEffect(crossinline action: suspend C.() -> Unit): Set<M> {
    action()
    return emptySet()
}

/**
 * Handy wrapper to perform side effect computations within coroutine scope
 *
 * @receiver command for which effect should be executed
 * @param C command
 * @param M message
 * @param action action to perform that might produce message to be consumed by a component
 * @return set of messages to be consumed a component
 */
suspend inline fun <C : Any, M : Any> C.effect(crossinline action: suspend C.() -> M?): Set<M> {
    return action(this@effect)?.let(::setOf) ?: emptySet()
}

/**
 * Returns flow that listens to changes of a component
 *
 * @receiver component for which [flow][Flow] of states should be returned
 * @param S state
 * @param M message
 * @return [Flow] of component states
 */
fun <M : Any, S : Any> Component<M, S>.changes(): Flow<S> = this(emptyFlow())

/**
 * Shortcut to supply multiple messages to the component without manually wrapping them into [flow][Flow]
 *
 * @receiver component that should receive specified messages
 * @param S state
 * @param M message
 * @param messages messages to supply
 * @return [Flow] of component states
 */
operator fun <M : Any, S : Any> Component<M, S>.invoke(vararg messages: M): Flow<S> = this(flowOf(*messages))

/**
 * Shortcut to supply a single message to the component without manually wrapping it into [flow][Flow]
 *
 * @receiver component that should receive specified messages
 * @param S state
 * @param M message
 * @param message message to supply
 * @return [Flow] of component states
 */
operator fun <M : Any, S : Any> Component<M, S>.invoke(message: M): Flow<S> = this(flowOf(message))

/**
 * Takes changes of the [producer] stream and feeds them as input to the [consumer] applying [transform] function
 *
 * @receiver coroutine scope to be used
 * @param producer the producer flow function
 * @param consumer the consumer flow function
 * @param transform function that maps produced states to the flow to be consumed by the consumer function
 */
inline fun <M1 : Any, S1 : Any, M2 : Any, S2 : Any> CoroutineScope.bind(noinline producer: Component<M1, S1>,
                                                                        noinline consumer: Component<M2, S2>,
                                                                        crossinline transform: (S1) -> Flow<M2>) {

    launch { producer.changes().collect { s1 -> launch { consumer(transform(s1)).collect() } } }
}

/**
 * Appends [interceptor][with] to this one
 *
 * @receiver interceptor for which another one should be appended
 * @param with interceptor to append
 * @param S state
 * @param M message
 * @param C command
 */
inline infix fun <M : Any, C : Any, S : Any> Interceptor<M, S, C>.with(crossinline with: Interceptor<M, S, C>): Interceptor<M, S, C> {
    return { message, prevState, newState, commands ->
        this(message, prevState, newState, commands); with(message, prevState, newState, commands)
    }
}