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
import io.github.xlopec.tea.async.Paginatable
import kotlinx.collections.immutable.PersistentList

/**
 * Pure dispatcher from a [Paginatable]'s state to one of five composable slots. Each state of
 * the sealed [Paginatable.State] hierarchy gets its own slot; no implicit wrapping, combining,
 * or branching on the size of [Paginatable.data].
 */
@Composable
@Suppress("FunctionName")
public fun <T : Any, Err> Paginatable(
    paginatable: Paginatable<T, Err>,
    onLoading: @Composable (data: PersistentList<T>) -> Unit,
    onRefreshing: @Composable (data: PersistentList<T>) -> Unit,
    onLoadingNext: @Composable (data: PersistentList<T>) -> Unit,
    onException: @Composable (error: Err, data: PersistentList<T>) -> Unit,
    onIdle: @Composable (data: PersistentList<T>) -> Unit,
) {
    when (val state = paginatable.state) {
        Paginatable.Loading -> onLoading(paginatable.data)
        Paginatable.Refreshing -> onRefreshing(paginatable.data)
        Paginatable.LoadingNext -> onLoadingNext(paginatable.data)
        is Paginatable.Exception -> onException(state.error, paginatable.data)
        Paginatable.Idle -> onIdle(paginatable.data)
    }
}

/**
 * [LazyListScope] variant of [Paginatable]. Each slot receives the surrounding [LazyListScope]
 * and is expected to emit its own `item`/`items` calls — the dispatcher itself does not wrap
 * content or combine slots. Callers compose the items + footer pattern for
 * [Paginatable.LoadingNext] and stale-data [Paginatable.Exception] themselves.
 */
public inline fun <T : Any, Err> LazyListScope.paginatableItems(
    paginatable: Paginatable<T, Err>,
    onLoading: LazyListScope.(data: PersistentList<T>) -> Unit,
    onRefreshing: LazyListScope.(data: PersistentList<T>) -> Unit,
    onLoadingNext: LazyListScope.(data: PersistentList<T>) -> Unit,
    onException: LazyListScope.(error: Err, data: PersistentList<T>) -> Unit,
    onIdle: LazyListScope.(data: PersistentList<T>) -> Unit,
) {
    when (val state = paginatable.state) {
        Paginatable.Loading -> onLoading(paginatable.data)
        Paginatable.Refreshing -> onRefreshing(paginatable.data)
        Paginatable.LoadingNext -> onLoadingNext(paginatable.data)
        is Paginatable.Exception -> onException(state.error, paginatable.data)
        Paginatable.Idle -> onIdle(paginatable.data)
    }
}
