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

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Snapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

typealias Interceptor<M, S, C> = suspend (snapshot: Snapshot<M, S, C>) -> Unit

/**
 * Handy extension to combine a single command with state
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param command command to combine with state
 * @return [UpdateWith] instance with given state and set that consists from a single command
 */
infix fun <S, C> S.command(
    command: C
): UpdateWith<S, C> = this to setOf(command)

/**
 * Handy extension to combine two commands with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S, C> S.command(
    first: C,
    second: C
): UpdateWith<S, C> = this to setOf(first, second)

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
fun <S, C> S.command(
    first: C,
    second: C,
    third: C
): UpdateWith<S, C> =
    this to setOf(first, second, third)

/**
 * Handy extension to combine multiple commands with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S, C> S.command(vararg commands: C): UpdateWith<S, C> = this command setOf(*commands)

/**
 * Handy extension to combine set of commands with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
infix fun <S, C> S.command(commands: Set<C>): UpdateWith<S, C> = this to commands

/**
 * Handy extension to express absence of commands to execute combined with state
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @return [UpdateWith] instance with given state and empty set of commands
 */
fun <S, C> S.noCommand(): UpdateWith<S, C> = this to emptySet()

/**
 * Handy wrapper to perform side effect computations within coroutine scope. This function always
 * returns empty set of messages [M]
 * @param action action to perform that produces no messages that can be consumed by a component
 * @return set of messages to be consumed by a component, always empty
 */
suspend inline infix fun <C, M> C.sideEffect(crossinline action: suspend C.() -> Unit): Set<M> {
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
suspend inline infix fun <C, M> C.effect(
    crossinline action: suspend C.() -> M?
): Set<M> = action(this@effect)?.let(::setOf) ?: emptySet()

fun <M, S, C> Component<M, S, C>.snapshotChanges(): Flow<Snapshot<M, S, C>> =
    this(emptyFlow())

fun <M, S, C> Component<M, S, C>.stateChanges(): Flow<S> =
    snapshotChanges().map { snapshot -> snapshot.currentState }

operator fun <M, S, C> Component<M, S, C>.invoke(vararg messages: M) =
    this(flowOf(*messages))

operator fun <M, S, C> Component<M, S, C>.invoke(messages: Iterable<M>) =
    this(messages.asFlow())

operator fun <M, S, C> Component<M, S, C>.invoke(message: M) =
    this(flowOf(message))

inline infix fun <M, S, C> Component<M, S, C>.with(
    crossinline interceptor: Interceptor<M, S, C>
): Component<M, S, C> =
    { input -> this(input).onEach { interceptor(it) } }

fun <M, S, C> Component<M, S, C>.states(): ((Flow<M>) -> Flow<S>) =
    { input -> this(input).map { snapshot -> snapshot.currentState } }