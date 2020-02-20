package com.oliynick.max.elm.core.component.internal

import com.oliynick.max.elm.core.component.InternalComponentApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*

@InternalComponentApi
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