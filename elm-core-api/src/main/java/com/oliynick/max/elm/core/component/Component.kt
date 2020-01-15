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

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package com.oliynick.max.elm.core.component

import kotlinx.coroutines.flow.Flow

/**
 * Alias for a pure function that accepts message with current state and returns the next state with possible empty set of commands
 * to feed [resolver][Resolver]
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 */
typealias Update<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>

/**
 * Alias for a function that resolves effects and returns messages to feed [update][Update] function
 * @param M incoming messages
 * @param C commands to be executed
 */
typealias Resolver<C, M> = suspend (command: C) -> Set<M>

/**
 * Alias for result of the [update][Update] function
 * @param S state of the component
 * @param C commands to be executed
 */
typealias UpdateWith<S, C> = Pair<S, Set<C>>

/**
 * Alias for a function that accepts input flow of messages and returns flow of states produced by that messages
 * @param M incoming messages
 * @param S state of the component
 */
@Deprecated("will be removed")
typealias ComponentLegacy<M, S> = (messages: Flow<M>) -> Flow<S>

typealias Component<M, S, C> = (messages: Flow<M>) -> Flow<Snapshot<M, S, C>>

/**
 * Alias for a function that loads initial state of the component and initial set of commands
 * @param C initial commands to execute
 * @param S initial state of the component
 */
@Deprecated("will be removed")
typealias InitializerLegacy<S, C> = suspend () -> InitArgs<S, C>

typealias Initializer<S, C> = suspend () -> Initial<S, C>

/**
 * Alias for result of the [init][InitializerLegacy] function
 * @param C initial commands to execute
 * @param S initial state of the component
 */
@Deprecated("will be removed", ReplaceWith("Initial()"))
typealias InitArgs<S, C> = Pair<S, Set<C>>

/**
 * Alias for a function that observes changes made inside component
 * @param M incoming message
 * @param C commands to be executed
 * @param S state of the component
 */
@Deprecated("will be removed", replaceWith = ReplaceWith("Interceptor"))
typealias LegacyInterceptor<M, S, C> = suspend (message: M, prevState: S, newState: S, commands: Set<C>) -> Unit

typealias Interceptor<M, S, C> = suspend (snapshot: Snapshot<M, S, C>) -> Unit

sealed class Snapshot<out M, out S, out C> {
    abstract val state: S
    abstract val commands: Set<C>
}

data class Initial<S, C>(
    override val state: S,
    override val commands: Set<C>
) : Snapshot<Nothing, S, C>()

data class Regular<M, S, C>(
    val message: M,
    override val state: S,
    override val commands: Set<C>
) : Snapshot<M, S, C>()
