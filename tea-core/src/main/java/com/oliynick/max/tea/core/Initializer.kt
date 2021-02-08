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

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core

/**
 * Initializer is an **impure** function that computes [initial][Initial] snapshot
 *
 * @param S initial state of the application
 * @param C initial set of commands to be executed
 */
public typealias Initializer<S, C> = suspend () -> Initial<S, C>

/**
 * Constructs initializer using initial [state] and set of [commands]
 *
 * @param state initial state
 * @param commands initial set of commands
 */
public fun <S, C> Initializer(
    state: S,
    commands: Set<C> = emptySet()
): Initializer<S, C> = { Initial(state, commands) }

/**
 * Constructs initializer using initial [state] and array of [commands]
 *
 * @param S state
 * @param C command
 * @param state initial state
 * @param commands initial set of commands
 */
public fun <S, C> Initializer(
    state: S,
    vararg commands: C
): Initializer<S, C> = Initializer(state, setOf(*commands))
