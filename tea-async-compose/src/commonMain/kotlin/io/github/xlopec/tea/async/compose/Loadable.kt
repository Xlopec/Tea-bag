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

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import io.github.xlopec.tea.async.Loadable

/**
 * Routes a [Loadable] to one of the supplied slots based on its current state.
 *
 * [Loadable.Idle] and [Loadable.Refreshing] both invoke [onIdle] because the visible payload is
 * the same; differentiate them inside [onIdle] via [Loadable.isRefreshing] if needed.
 *
 * @param onLoading slot rendered when the producer is performing the initial load. No data is
 *   passed because [Loadable.data] in this state is a placeholder.
 * @param onException slot rendered when the producer is in the [Loadable.Exception] state. The
 *   state and the last observed [Loadable.data] are forwarded so the slot can show a banner over
 *   stale content or a full-screen error.
 * @param onIdle slot rendered when the producer is [Loadable.Idle] or [Loadable.Refreshing].
 */
@Composable
@Suppress("FunctionName")
public fun <T, Err> Loadable(
    loadable: Loadable<T, Err>,
    onLoading: @Composable () -> Unit,
    onException: @Composable (state: Loadable.Exception<Err>, data: T) -> Unit,
    onIdle: @Composable (data: T) -> Unit,
) {
    when (val state = loadable.state) {
        Loadable.Loading -> onLoading()
        is Loadable.Exception -> onException(state, loadable.data)
        Loadable.Idle, Loadable.Refreshing -> onIdle(loadable.data)
    }
}

/**
 * [LazyListScope] variant of [Loadable] that wraps transient states ([Loadable.Loading],
 * [Loadable.Exception]) in a single `item { }` and delegates the data-bearing
 * [Loadable.Idle]/[Loadable.Refreshing] state to [onIdle], which receives a [LazyListScope] and
 * is expected to emit its own `item`/`items` calls.
 *
 * Each transient state uses a distinct item key so Compose treats them as different items;
 * `remember`-backed state inside a slot is discarded on transition and item-animations fire
 * correctly.
 */
public inline fun <T, Err> LazyListScope.loadableItems(
    loadable: Loadable<T, Err>,
    crossinline onLoading: @Composable LazyItemScope.() -> Unit,
    crossinline onException: @Composable LazyItemScope.(state: Loadable.Exception<Err>, data: T) -> Unit,
    onIdle: LazyListScope.(data: T) -> Unit,
) {
    when (val state = loadable.state) {
        Loadable.Loading -> item(key = Loadable.Loading::class.simpleName) { onLoading() }
        is Loadable.Exception -> item(key = Loadable.Exception::class.simpleName) { onException(state, loadable.data) }
        Loadable.Idle, Loadable.Refreshing -> onIdle(loadable.data)
    }
}
