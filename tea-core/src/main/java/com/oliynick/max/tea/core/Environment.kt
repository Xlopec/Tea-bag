/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
public data class Env<M, S, C>(
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
public data class ShareOptions(
    val started: SharingStarted,
    val replay: UInt,
)

@UnstableApi
public val ShareStateWhileSubscribed: ShareOptions =
    ShareOptions(SharingStarted.WhileSubscribed(), 1U)
