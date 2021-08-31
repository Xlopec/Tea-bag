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

package com.oliynick.max.tea.core.debug.app.misc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

inline fun <M, S1, S2> ((Flow<M>) -> Flow<S1>).mapS(
    crossinline mapper: (S1) -> S2
): ((Flow<M>) -> Flow<S2>) = mapNullableS(mapper)

inline fun <M, S1, S2> ((Flow<M>) -> Flow<S1>).mapNullableS(
    crossinline mapper: (S1) -> S2?
): ((Flow<M>) -> Flow<S2>) =
    { input -> this(input).mapNotNull { s1 -> mapper(s1) }}
