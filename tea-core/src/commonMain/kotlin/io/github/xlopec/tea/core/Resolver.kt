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

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Alias for a possibly **impure** function that resolves commands to messages and performs side
 * effects.
 *
 * ### Exceptions
 *
 * Any exception that happens inside this function will redelivered to a [Component]'s scope and handled
 * by it. For more information regarding error handling see [shareIn][kotlinx.coroutines.flow.shareIn]
 *
 */
public typealias Resolver<M, S, C> = (snapshot: Snapshot<M, S, C>, context: ResolveCtx<M>) -> Unit

/**
 * This class represents a resolver context. [sink] and [scope] are used to resolve effects.
 * [sink] should be used to consume resolved messages and [scope] should be used to launch long-running operations.
 * Do ***not*** store references to [sink] or [scope] since they might be different between resolver invocations
 */
// todo replace with multi receivers
public data class ResolveCtx<in M> internal constructor(
    public val sink: Sink<M>,
    public val scope: CoroutineScope,
)

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
public infix fun <M> ResolveCtx<M>.effects(
    action: suspend () -> Set<M>,
): Job {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return scope.launch { sink(action()) }
}

/**
 * Resolves [action] to set of messages using provided [resolver context][ResolveCtx]
 */
@ExperimentalTeaApi
public inline infix fun <M> ResolveCtx<M>.effect(
    crossinline action: suspend () -> M?,
): Job {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return scope.launch { action()?.also { sink(it) } }
}

/**
 * Resolves [action] to empty set of messages using provided [resolver context][ResolveCtx]
 */
@ExperimentalTeaApi
public inline infix fun <M> ResolveCtx<M>.sideEffect(
    crossinline action: suspend () -> Unit,
): Job {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return scope.launch { action() }
}

public suspend operator fun <T> Sink<T>.invoke(
    elements: Iterable<T>,
): Unit = elements.forEach { t -> invoke(t) }

public suspend operator fun <T> Sink<T>.invoke(
    vararg elements: T,
): Unit = elements.forEach { t -> invoke(t) }
