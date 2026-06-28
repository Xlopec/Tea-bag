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

package io.github.xlopec.tea.async.compose

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import io.github.xlopec.tea.async.Loadable

/**
 * Pure dispatcher from a [Loadable]'s state to one of four composable slots. Each state of the
 * sealed [Loadable.State] hierarchy gets its own slot; no implicit collapsing, no empty-data
 * branching — the caller owns every visual decision.
 */
@Composable
@Suppress("FunctionName")
public fun <T, Err> Loadable(
    loadable: Loadable<T, Err>,
    onLoading: @Composable (data: T) -> Unit,
    onRefreshing: @Composable (data: T) -> Unit,
    onException: @Composable (error: Err, data: T) -> Unit,
    onIdle: @Composable (data: T) -> Unit,
) {
    when (val state = loadable.state) {
        Loadable.Loading -> onLoading(loadable.data)
        Loadable.Refreshing -> onRefreshing(loadable.data)
        is Loadable.Exception -> onException(state.error, loadable.data)
        Loadable.Idle -> onIdle(loadable.data)
    }
}

/**
 * [LazyListScope] variant of [Loadable]. Each slot receives the surrounding [LazyListScope] and
 * is expected to emit its own `item`/`items` calls — the dispatcher itself does not wrap content
 * or combine slots.
 */
public inline fun <T, Err> LazyListScope.loadableItems(
    loadable: Loadable<T, Err>,
    onLoading: LazyListScope.(data: T) -> Unit,
    onRefreshing: LazyListScope.(data: T) -> Unit,
    onException: LazyListScope.(error: Err, data: T) -> Unit,
    onIdle: LazyListScope.(data: T) -> Unit,
) {
    when (val state = loadable.state) {
        Loadable.Loading -> onLoading(loadable.data)
        Loadable.Refreshing -> onRefreshing(loadable.data)
        is Loadable.Exception -> onException(state.error, loadable.data)
        Loadable.Idle -> onIdle(loadable.data)
    }
}
