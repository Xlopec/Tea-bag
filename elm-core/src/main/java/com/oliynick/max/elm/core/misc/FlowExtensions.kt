package com.oliynick.max.elm.core.misc

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

fun <T> Flow<T>.mergeWith(other: Flow<T>): Flow<T> =
    channelFlow {
        coroutineScope {
            launch {
                other.collect {
                    offer(it)
                }
            }

            launch {
                collect {
                    offer(it)
                }
            }
        }
    }

fun <T> Flow<T>.startWith(t: T): Flow<T> = onStart { emit(t) }

fun <T> Flow<T>.startWith(first: T, vararg other: T): Flow<T> = onStart { emit(first); other.forEach { emit(it) } }

/** forces compiler to check `when` clause is exhaustive */
val Unit?.safe get() = this