package com.oliynick.max.tea.core.debug.app.misc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

inline fun <M, S1, S2> ((Flow<M>) -> Flow<S1>).mapS(
    crossinline mapper: (S1) -> S2
): ((Flow<M>) -> Flow<S2>) = flatMapS(mapper)

inline fun <M, S1, S2> ((Flow<M>) -> Flow<S1>).flatMapS(
    crossinline mapper: (S1) -> S2?
): ((Flow<M>) -> Flow<S2>) =
    { input -> this(input).mapNotNull { s1 -> mapper(s1) }}
