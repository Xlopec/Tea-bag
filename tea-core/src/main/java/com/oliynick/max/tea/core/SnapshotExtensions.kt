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

package com.oliynick.max.tea.core

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <S> Snapshot<*, S, *>.component1(): S = when (this) {
    is Initial -> currentState
    is Regular -> currentState
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <C> Snapshot<*, *, C>.component2(): Set<C> = when (this) {
    is Initial -> commands
    is Regular -> commands
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <S> Snapshot<*, S, *>.component3(): S? = when (this) {
    is Initial -> null
    is Regular -> previousState
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <M> Snapshot<M, *, *>.component4(): M? = when (this) {
    is Initial -> null
    is Regular -> message
}
