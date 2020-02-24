package com.oliynick.max.tea.core.debug.component.internal

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal fun <T> Flow<T>.mergeWith(
    another: Flow<T>
): Flow<T> = channelFlow {
    coroutineScope {
        launch {
            another.collect {
                send(it)
            }
        }

        launch {
            collect {
                send(it)
            }
        }
    }
}