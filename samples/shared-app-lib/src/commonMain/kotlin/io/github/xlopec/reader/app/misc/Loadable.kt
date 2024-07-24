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

package io.github.xlopec.reader.app.misc

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.feature.article.list.Page
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

public data class Loadable<out T>(
    val data: PersistentList<T>,
    val hasMore: Boolean,
    val loadableState: LoadableState,
) {
    public companion object {
        public fun <T> newLoading(
            data: PersistentList<T> = persistentListOf()
        ): Loadable<T> = Loadable(data = data, hasMore = false, loadableState = Loading)
    }
}

public sealed interface LoadableState

public data class Exception(
    val th: AppException,
) : LoadableState

public object Loading : LoadableState

public object LoadingNext : LoadableState

public object Refreshing : LoadableState

public object Idle : LoadableState

public val Loadable<*>.isLoading: Boolean
    get() = loadableState === Loading

public val Loadable<*>.isLoadingNext: Boolean
    get() = loadableState === LoadingNext

public val Loadable<*>.isRefreshing: Boolean
    get() = loadableState === Refreshing

public val Loadable<*>.isIdle: Boolean
    get() = loadableState === Idle || isException

public val Loadable<*>.isException: Boolean
    get() = loadableState is Exception

public inline fun <T> Loadable<T>.updated(
    how: PersistentList<T>.() -> PersistentList<T>
): Loadable<T> = copy(data = how(data))

public fun <T> Loadable<T>.toLoadingNext(): Loadable<T> {
    checkCanLoadNextPage()
    return copy(loadableState = LoadingNext)
}

public fun <T> Loadable<T>.toLoading(): Loadable<T> =
    copy(loadableState = Loading)

public fun <T> Loadable<T>.toRefreshing(): Loadable<T> =
    copy(loadableState = Refreshing)

public fun <T> Loadable<T>.toIdle(
    page: Page<T>,
): Loadable<T> =
    when (loadableState) {
        LoadingNext, is Exception -> copy(
            data = data.addAll(page.data),
            loadableState = Idle,
            hasMore = page.hasMore
        )
        Loading, Refreshing -> copy(
            data = page.data.toPersistentList(),
            loadableState = Idle,
            hasMore = page.hasMore
        )
        Idle -> this
    }

public fun <T> Loadable<T>.toException(
    cause: AppException,
): Loadable<T> =
    when (loadableState) {
        LoadingNext, is Exception, Idle -> copy(loadableState = Exception(cause))
        Loading, Refreshing -> Loadable(
            data = persistentListOf(),
            loadableState = Exception(cause),
            hasMore = false
        )
    }

private fun Loadable<*>.checkCanLoadNextPage() {
    require(data.isNotEmpty()) {
        "$this doesn't contain items and thus LoadingNext transition is prohibited. Request full reload instead"
    }
}
