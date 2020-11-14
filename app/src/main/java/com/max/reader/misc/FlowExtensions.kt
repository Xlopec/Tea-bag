package com.max.reader.misc

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

suspend inline fun <T> Flow<T>.collect(
    dispatcher: CoroutineDispatcher,
    crossinline collector: (T) -> Unit
) {
    collect { t ->
        withContext(dispatcher) {
            collector(t)
        }
    }
}