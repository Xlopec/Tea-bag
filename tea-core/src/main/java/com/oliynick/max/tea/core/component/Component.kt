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
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.EnvBuilder
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.UnstableApi
import com.oliynick.max.tea.core.component.internal.downstream
import com.oliynick.max.tea.core.component.internal.init
import com.oliynick.max.tea.core.component.internal.shareConflated
import com.oliynick.max.tea.core.component.internal.upstream
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

typealias Component<M, S, C> = (messages: Flow<M>) -> Flow<Snapshot<M, S, C>>

/**
 * Alias for a pure function that accepts message with current state and returns the next state with possible empty set of commands
 * to feed [resolver][Resolver]
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 */
typealias Updater<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>

/**
 * Alias for a function that resolves effects and returns messages to feed [update][Updater] function
 * @param M incoming messages
 * @param C commands to be executed
 */
typealias Resolver<C, M> = suspend (command: C) -> Set<M>

/**
 * Alias for result of the [update][Updater] function
 * @param S state of the component
 * @param C commands to be executed
 */
typealias UpdateWith<S, C> = Pair<S, Set<C>>

fun <M, C, S> Component(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
): Component<M, S, C> = Component(Env(initializer, resolver, updater, config))

fun <M, S, C> Component(
    env: Env<M, S, C>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input.consumeAsFlow(), env.init()).shareConflated()

    return { messages -> upstream.downstream(messages, input) }
}

