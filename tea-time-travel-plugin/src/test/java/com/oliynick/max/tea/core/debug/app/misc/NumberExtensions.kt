package com.oliynick.max.tea.core.debug.app.misc

inline fun <T> Int.times(
    block: () -> T
): List<T> = (0 until this).map { block() }