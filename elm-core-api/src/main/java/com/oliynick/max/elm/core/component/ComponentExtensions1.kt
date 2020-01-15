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

import kotlinx.coroutines.flow.*

fun <M, S, C> Component<M, S, C>.snapshotChanges(): Flow<Snapshot<M, S, C>> =
    this(emptyFlow())

fun <M, S, C> Component<M, S, C>.stateChanges(): Flow<S> =
    snapshotChanges().map { snapshot -> snapshot.state }

operator fun <M, S, C> Component<M, S, C>.invoke(vararg messages: M) =
    this(flowOf(*messages))

operator fun <M, S, C> Component<M, S, C>.invoke(messages: Iterable<M>) =
    this(messages.asFlow())

operator fun <M, S, C> Component<M, S, C>.invoke(message: M) =
    this(flowOf(message))

inline infix fun <M, S, C> Component<M, S, C>.with(
    crossinline interceptor: Interceptor<M, S, C>
): Component<M, S, C> =
    { input -> this(input).onEach { interceptor(it) } }

fun <M, S, C> Component<M, S, C>.states(): ((Flow<M>) -> Flow<S>) =
    { input -> this(input).map { snapshot -> snapshot.state } }
