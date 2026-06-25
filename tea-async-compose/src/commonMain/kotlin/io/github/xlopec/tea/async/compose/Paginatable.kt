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
import io.github.xlopec.tea.async.Paginatable
import kotlinx.collections.immutable.PersistentList

/**
 * Routes a [Paginatable] to one of the supplied slots based on its current state.
 *
 * Transient states ([Paginatable.Loading], full-screen [Paginatable.Exception]) are emitted as
 * a single `item { }`. [Paginatable.LoadingNext] and [Paginatable.Exception] with existing
 * items render the items via [onIdle] and append the transient slot as a footer item so
 * pagination indicators sit at the end of the list.
 *
 * @param onLoading slot rendered as a single item when the initial load is in progress.
 * @param onException slot rendered when the producer is in the [Paginatable.Exception] state.
 *   Emitted as a footer when items already exist, or as the sole item when the list is empty.
 * @param onLoadingNext slot appended as a footer item while [Paginatable.LoadingNext] is in
 *   progress.
 * @param onIdle slot for [Paginatable.Idle] and [Paginatable.Refreshing]; receives the
 *   [LazyListScope] so the caller can emit `items` directly.
 */
public inline fun <T : Any, Err> LazyListScope.paginatableItems(
    paginatable: Paginatable<T, Err>,
    crossinline onLoading: @Composable LazyItemScope.() -> Unit,
    crossinline onException: @Composable LazyItemScope.(Paginatable.Exception<Err>, PersistentList<T>) -> Unit,
    crossinline onLoadingNext: @Composable LazyItemScope.() -> Unit,
    onIdle: LazyListScope.(data: PersistentList<T>) -> Unit,
) {
    when (val state = paginatable.state) {
        Paginatable.Loading -> item(key = Paginatable.Loading::class.simpleName) { onLoading() }

        is Paginatable.Exception -> {
            if (paginatable.data.isNotEmpty()) {
                onIdle(paginatable.data)
            }
            item(key = Paginatable.Exception::class.simpleName) { onException(state, paginatable.data) }
        }

        Paginatable.LoadingNext -> {
            onIdle(paginatable.data)
            item(key = Paginatable.LoadingNext::class.simpleName) { onLoadingNext() }
        }

        Paginatable.Idle, Paginatable.Refreshing -> onIdle(paginatable.data)
    }
}
