/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.elm.core.component

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

fun <M, S, C> Component1<M, S, C>.changes() = this(emptyFlow())

operator fun <M, S, C> Component1<M, S, C>.invoke(vararg messages: M) = this(flowOf(*messages))

operator fun <M, S, C> Component1<M, S, C>.invoke(message: M) = this(flowOf(message))

inline infix fun <M, S, C> Component1<M, S, C>.with(
    crossinline interceptor: Interceptor<M, S, C>
): Component1<M, S, C> = { input -> this(input).onEach { interceptor(it) } }
