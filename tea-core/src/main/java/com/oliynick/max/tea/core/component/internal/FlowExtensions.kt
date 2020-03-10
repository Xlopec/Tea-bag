package com.oliynick.max.tea.core.component.internal

import com.oliynick.max.tea.core.UnstableApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

@UnstableApi
suspend fun <T> Flow<T>.into(
    sendChannel: SendChannel<T>
) = collect(sendChannel::send)

internal fun <T> Flow<T>.finishWith(
    flow: Flow<T>
) = onCompletion { th -> if (th != null) throw th else emitAll(flow) }

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