/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * Defines transformation of an upstream [Flow] into a resulting Component's [Flow].
 *
 * This function is used for configuring how the upstream flow should be shared,
 * with particular lifecycle behavior, within a given [CoroutineScope].
 *
 * @param T the type of elements emitted by the [Flow]
 * @return a configured [Flow] with the desired behavior
 */
public typealias ShareOptions<T> = (scope: CoroutineScope, upstream: Flow<T>) -> Flow<T>

/**
 * Provides sharing options for a state flow, utilizing the `shareIn` operator with a `WhileSubscribed` strategy.
 *
 * @param started Determines the sharing strategy, defaulting to `SharingStarted.WhileSubscribed()`.
 * @param replay The number of replayed values, defaulting to 1.
 * @return A lambda function that applies the `shareIn` operator to the upstream flow using the provided parameters.
 */
@Suppress("FunctionName")
public fun <T> ShareStateWhileSubscribed(
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    replay: UInt = 1U,
): ShareOptions<T> = { scope, upstream -> upstream.shareIn(scope = scope, started = started, replay = replay.toInt()) }
