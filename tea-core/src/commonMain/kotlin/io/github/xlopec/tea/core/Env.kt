/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.tea.core

import kotlinx.coroutines.CoroutineScope

/**
 * Environment is an application component responsible for holding application dependencies
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param scope scope in which the sharing coroutine is started
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param M message type
 * @param S state type
 * @param C command type
 */
public data class Env<M, S, C>(
    val initializer: Initializer<S, C>,
    val resolver: Resolver<M, S, C>,
    val updater: Updater<M, S, C>,
    // todo: group to reduce number of arguments
    val scope: CoroutineScope,
    val shareOptions: ShareOptions = ShareStateWhileSubscribed,
)
