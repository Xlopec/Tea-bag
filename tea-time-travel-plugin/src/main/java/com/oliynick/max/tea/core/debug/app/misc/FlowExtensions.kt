package com.oliynick.max.tea.core.debug.app.misc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

fun <T> Flow<T>.onStart(
    t: T
) = onStart { emit(t) }
