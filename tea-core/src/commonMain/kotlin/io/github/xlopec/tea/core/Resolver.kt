/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Alias for a possibly **impure** function that resolves commands to messages and performs side
 * effects.
 *
 * ### Exceptions
 *
 * Any exception that happens inside this function will be delivered to a [Component]'s scope and handled
 * by it. For more information regarding error handling see [shareIn][kotlinx.coroutines.flow.shareIn]
 *
 */
public typealias Resolver<M, S, C> = (snapshot: Snapshot<M, S, C>, context: ResolveCtx<M>) -> Unit

/**
 * This class represents a resolver context that used to resolve effects.
 * To consume resolved messages use [ResolveCtx.invoke]. This class implements [CoroutineScope] to launch long-running component lifecycle
 * aware operations.
 * Do ***not*** store reference to [ResolveCtx] since it might change between invocations
 */
// todo replace with multi receivers
public class ResolveCtx<in M> internal constructor(
    sink: Sink<M>,
    scope: CoroutineScope,
) : CoroutineScope by scope, Sink<M> by sink

/**
 * Type alias for suspending function that accepts incoming values a puts it to a queue for later
 * processing
 *
 * @param T incoming values
 */
public typealias Sink<T> = suspend (T) -> Unit

/**
 * Resolves [action] to set of messages using provided [resolver context][ResolveCtx]
 */
@ExperimentalTeaApi
public inline infix fun <M> ResolveCtx<M>.effects(
    crossinline action: suspend () -> Set<M>,
): Job {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    return launch { invoke(action()) }
}

/**
 * Resolves [action] to set of messages using provided [resolver context][ResolveCtx]
 */
@ExperimentalTeaApi
public inline infix fun <M> ResolveCtx<M>.effect(
    crossinline action: suspend () -> M?,
): Job {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    return launch { action()?.also { invoke(it) } }
}

/**
 * Resolves [action] to empty set of messages using provided [resolver context][ResolveCtx]
 */
@ExperimentalTeaApi
public inline infix fun <M> ResolveCtx<M>.sideEffect(
    crossinline action: suspend () -> Unit,
): Job {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    return launch { action() }
}

public suspend operator fun <T> Sink<T>.invoke(
    elements: Iterable<T>,
): Unit = elements.forEach { t -> invoke(t) }

public suspend operator fun <T> Sink<T>.invoke(
    vararg elements: T,
): Unit = elements.forEach { t -> invoke(t) }
