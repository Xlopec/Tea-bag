/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

package com.oliynick.max.tea.core.component.internal

import com.oliynick.max.tea.core.UnstableApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@UnstableApi
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
