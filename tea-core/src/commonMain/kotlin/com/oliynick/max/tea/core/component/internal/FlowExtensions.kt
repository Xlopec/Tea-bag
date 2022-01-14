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

package com.oliynick.max.tea.core.component.internal

import com.oliynick.max.tea.core.InternalTeaApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Deprecated("Bad api")
@InternalTeaApi
public suspend fun <T> Flow<T>.into(
    sendChannel: SendChannel<T>
) {
    collect(sendChannel::send)
}

internal fun <T> Flow<T>.finishWith(
    flow: Flow<T>
) = onCompletion { th -> if (th != null) throw th else emitAll(flow) }

internal fun <T> Flow<T>.mergeWith(other: Flow<T>): Flow<T> =
    channelFlow {

        launch {
            other.collect {
                send(it)
            }
        }

        launch {
            collect {
                send(it)
            }
        }
    }

internal inline fun <T, R> Flow<T>.foldFlatten(
    acc: R,
    crossinline transform: suspend (R, T) -> Flow<R>
): Flow<R> {

    var current = acc

    return flatMapConcat { next ->
        transform(current, next)
            .onEach { new -> current = new }
    }
}

internal fun <T> Flow<T>.startFrom(
    t: T
) = onStart { emit(t) }

internal suspend fun <T> FlowCollector<T>.emitAll(
    elements: Iterable<T>
) = elements.forEach { element -> emit(element) }
