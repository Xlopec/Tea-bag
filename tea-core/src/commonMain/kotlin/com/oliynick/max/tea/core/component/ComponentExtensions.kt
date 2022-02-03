/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.ExperimentalTeaApi
import com.oliynick.max.tea.core.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

/**
 * **Impure** function that performs some actions on snapshots
 * @param M message
 * @param S state
 * @param C command
 */
public typealias Interceptor<M, S, C> = suspend (snapshot: Snapshot<M, S, C>) -> Unit

/**
 * Extension to combine state with command
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param C command
 * @param command command to combine with state
 * @return [UpdateWith] instance with given state and set that consists from a single command
 */
public infix fun <S, C> S.command(
    command: C,
): UpdateWith<S, C> = this to setOf(command)

/**
 * Extension to combine state with a single command provider
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param C command
 * @param command command to combine with state
 * @return [UpdateWith] instance with given state and set that consists from a single command
 */
public inline infix fun <S, C> S.command(
    command: S.() -> C,
): UpdateWith<S, C> = this to setOf(run(command))

/**
 * Extension to combine state with two commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
public fun <S, C> S.command(
    first: C,
    second: C,
): UpdateWith<S, C> = this to setOf(first, second)

/**
 * Extension to combine state with three commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @param third the third command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
public fun <S, C> S.command(
    first: C,
    second: C,
    third: C,
): UpdateWith<S, C> =
    this to setOf(first, second, third)

/**
 * Extension to combine state with multiple commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
public fun <S, C> S.command(
    vararg commands: C,
): UpdateWith<S, C> = this command setOf(*commands)

/**
 * Extension to combine state with set of commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
public infix fun <S, C> S.command(
    commands: Set<C>,
): UpdateWith<S, C> = this to commands

/**
 * Extension to combine state with empty set of commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @return [UpdateWith] instance with given state and empty set of commands
 */
public fun <S> S.noCommand(): UpdateWith<S, Nothing> = this to emptySet()

/**
 * Wrapper to perform **only** side effect using command as receiver. This function always returns
 * empty set of messages
 * @param action action to perform that produces no messages that can be consumed by a component
 * @param C command
 * @param M message
 * @return set of messages to be consumed by a component, always empty
 */
public suspend inline infix fun <C, M> C.sideEffect(
    crossinline action: suspend C.() -> Unit,
): Set<M> {
    contract {
        callsInPlace(action, EXACTLY_ONCE)
    }
    action()
    return setOf()
}

/**
 * Wrapper to perform side effect computations and possibly return a new message to be consumed by [Updater]
 *
 * @receiver command to be used to execute effect
 * @param C command
 * @param M message
 * @param action action to perform that might produce message to be consumed by a component
 * @return set of messages to be consumed a component
 */
public suspend inline infix fun <C, M> C.effect(
    crossinline action: suspend C.() -> M?,
): Set<M> {
    contract {
        callsInPlace(action, EXACTLY_ONCE)
    }
    return setOfNotNull(action(this))
}

/**
 * Transforms component into flow of snapshots
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public fun <M, S, C> Component<M, S, C>.observeSnapshots(): Flow<Snapshot<M, S, C>> =
    this(emptyFlow())

/**
 * Transforms component into flow of states
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public fun <M, S, C> Component<M, S, C>.observeStates(): Flow<S> =
    observeSnapshots().map { snapshot -> snapshot.currentState }

/**
 * Transforms component into function that accepts messages and returns flow that
 * emits states only
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public fun <M, S, C> Component<M, S, C>.states(): ((Flow<M>) -> Flow<S>) =
    { input -> this(input).map { snapshot -> snapshot.currentState } }

/**
 * Supplies [messages] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public operator fun <M, S, C> Component<M, S, C>.invoke(
    vararg messages: M,
): Flow<Snapshot<M, S, C>> = this(flowOf(*messages))

/**
 * Supplies [messages] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public operator fun <M, S, C> Component<M, S, C>.invoke(
    messages: Iterable<M>,
): Flow<Snapshot<M, S, C>> = this(messages.asFlow())

/**
 * Supplies [message] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public operator fun <M, S, C> Component<M, S, C>.invoke(
    message: M,
): Flow<Snapshot<M, S, C>> = this(flowOf(message))

/**
 * Attaches [interceptor] to the component
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
public infix fun <M, S, C> Component<M, S, C>.with(
    interceptor: Interceptor<M, S, C>,
): Component<M, S, C> =
    { input -> this(input).onEach(interceptor) }

/**
 * Establishes subscription in ELM's terminology. It allows component
 * to listen to external messages
 *
 * @param input external messages
 * @param scope scope that will be used to manage subscription
 * @return [Job] if subscription needs to be managed manually
 */
@ExperimentalTeaApi
public fun <M> ((Flow<M>) -> Flow<*>).subscribeIn(
    input: Flow<M>,
    scope: CoroutineScope
): Job = scope.launch { invoke(input).collect() }
