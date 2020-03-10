package com.oliynick.max.tea.core.component.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal suspend inline fun <T, R> Iterable<T>.parMapTo(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline mapper: suspend (T) -> R
) = coroutineScope { map { t -> async(dispatcher) { mapper(t) } }.awaitAll() }
