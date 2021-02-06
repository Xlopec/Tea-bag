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
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core

import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Updater
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted

/**
 * Environment is an application component responsible for holding application dependencies
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param scope scope in which the sharing coroutine is started
 * @param io coroutine dispatcher which is used to execute side effects by [resolver]
 * @param computation coroutine dispatcher which is used to wrap [updater]'s computations
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param M message type
 * @param S state type
 * @param C command type
 */
data class Env<M, S, C>(
    val initializer: Initializer<S, C>,
    val resolver: Resolver<C, M>,
    val updater: Updater<M, S, C>,
    // todo: group to reduce number of arguments
    val scope: CoroutineScope,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val computation: CoroutineDispatcher = Dispatchers.Unconfined,
    val shareOptions: ShareOptions = ShareStateWhileSubscribed,
)

/**
 * Share options used to configure the sharing coroutine
 *
 * @param started sharing strategy
 * @param replay number of states to be replayed to the downstream subscribers
 * @see [SharingStarted]
 */
data class ShareOptions(
    val started: SharingStarted,
    val replay: UInt,
)

@UnstableApi
val ShareStateWhileSubscribed = ShareOptions(SharingStarted.WhileSubscribed(), 1U)
