/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

package io.github.xlopec.tea.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Alias for a possibly **impure** function that resolves commands to messages and performs side
 * effects.
 *
 * ### Exceptions
 *
 * Any exception that happens inside this function will be delivered to a [Component]'s scope and handled
 * by it. For more information regarding error handling, see [shareIn][kotlinx.coroutines.flow.shareIn].
 *
 */
public typealias Resolver<M, S, C> = context(Sink<M>, CoroutineScope) (snapshot: Flow<Snapshot<M, S, C>>) -> Unit

/**
 * Type alias for a suspending function that accepts incoming values and puts them into a queue for later
 * processing.
 *
 * @param T incoming values type
 */
public typealias Sink<T> = suspend (T) -> Unit

/**
 * Resolves [action] that returns a set of messages.
 *
 * @param M message type
 * @param action suspending function that returns a set of messages to be consumed
 * @return job that performs [action]
 */
@ExperimentalTeaApi
context(_: Sink<M>, _: CoroutineScope)
public infix fun <M, C> C.effects(
    action: suspend C.() -> Set<M>,
): Job = effects(EmptyCoroutineContext, CoroutineStart.DEFAULT, action)

/**
 * Resolves [action] that returns a single message.
 *
 * @param M message type
 * @param action suspending function that returns a message to be consumed, or `null` if no message should be consumed
 * @return job that performs [action]
 */
@ExperimentalTeaApi
context(sink: Sink<M>, scope: CoroutineScope)
public fun <M, C> C.effects(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend C.() -> Set<M>,
): Job {
    return scope.launch(context, start) { sink(action()) }
}

/**
 * Resolves [action] that returns a single message.
 *
 * @param M message type
 * @param action suspending function that returns a message to be consumed, or `null` if no message should be consumed
 * @return job that performs [action]
 */
@ExperimentalTeaApi
context(_: Sink<M>, _: CoroutineScope)
public infix fun <M, C> C.effect(
    action: suspend C.() -> M?,
): Job = effect(EmptyCoroutineContext, CoroutineStart.DEFAULT, action)

/**
 * Resolves [action] and emits message to the [sink] if any
 */
@ExperimentalTeaApi
context(sink: Sink<M>, scope: CoroutineScope)
public fun <M, C> C.effect(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend C.() -> M?,
): Job {
    return scope.launch(context, start) { action()?.also { sink(it) } }
}

/**
 * Resolves [action] that doesn't return any messages.
 *
 * @param M message type
 * @param action suspending function that performs a side effect
 * @return job that performs [action]
 */
@ExperimentalTeaApi
context(_: Sink<M>, _: CoroutineScope)
public infix fun <M, C> C.sideEffect(
    action: suspend C.() -> Unit,
): Job = sideEffect(EmptyCoroutineContext, CoroutineStart.DEFAULT, action)

/**
 * Resolves [action] that doesn't return any messages.
 *
 * @param M message type
 * @param action suspending function that performs a side effect
 * @return job that performs [action]
 */
@ExperimentalTeaApi
context(_: Sink<M>, scope: CoroutineScope)
public fun <M, C> C.sideEffect(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend C.() -> Unit,
): Job {
    return scope.launch(context, start) { action() }
}

/**
 * Consumes [elements].
 *
 * @param T element type
 * @param elements messages to be consumed
 */
public suspend operator fun <T> Sink<T>.invoke(
    elements: Iterable<T>,
): Unit = elements.forEach { t -> invoke(t) }

/**
 * Consumes [elements].
 *
 * @param T element type
 * @param elements messages to be consumed
 */
public suspend operator fun <T> Sink<T>.invoke(
    vararg elements: T,
): Unit = elements.forEach { t -> invoke(t) }
