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

package io.github.xlopec.tea.data

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Container that wraps a paginated list of values together with the [State] of the operation
 * that produces them.
 *
 * Use [Paginatable] when items are fetched page-by-page (typically as the user scrolls).
 * For non-paginated values prefer [Loadable].
 *
 * The lifecycle distinguishes [Loading] (initial fetch with an empty list), [LoadingNext]
 * (subsequent page appended to an already-loaded list), [Refreshing] (re-fetch of the first
 * page), [Idle] and a terminal [Exception] state. State transitions are encoded as `to…`
 * extensions and a successful page fetch is recorded via [toIdle].
 *
 * @param T element type; must be non-nullable so list operations remain well-defined
 * @param Err type of the error produced when a fetch fails.
 * @property data items observed so far
 * @property hasMore `true` if the source can deliver additional pages
 * @property state lifecycle of the producer
 */
public data class Paginatable<out T : Any, out Err>(
    val data: PersistentList<T>,
    val hasMore: Boolean,
    val state: State<Err>,
) {
    public companion object {

        /**
         * Creates a [Loading] [Paginatable] holding [data] (empty by default).
         */
        public fun <T : Any> loadingList(
            data: PersistentList<T> = persistentListOf(),
        ): Paginatable<T, Nothing> = Paginatable(data = data, hasMore = false, state = Loading)

        /**
         * Creates an [Idle] [Paginatable] holding [data] (empty by default).
         */
        public fun <T : Any> idleList(
            data: PersistentList<T> = persistentListOf(),
        ): Paginatable<T, Nothing> = Paginatable(data = data, hasMore = false, state = Idle)
    }

    /**
     * Lifecycle of the paginated producer.
     *
     * @param Err type of error reported by [Exception]; [Loading], [LoadingNext],
     *   [Refreshing] and [Idle] carry no error data and are typed as `State<Nothing>`.
     */
    public sealed interface State<out Err>

    /**
     * Terminal failure state.
     *
     * @property error cause of the failure
     */
    @OptIn(ExperimentalObjCName::class)
    @ObjCName("Error")
    public data class Exception<out Err>(
        @param:ObjCName("error") val error: Err,
    ) : State<Err>

    /**
     * Initial load is in progress; [data] is typically empty.
     */
    public data object Loading : State<Nothing>

    /**
     * An additional page is being appended to an already non-empty [data].
     */
    public data object LoadingNext : State<Nothing>

    /**
     * A full re-fetch of the first page is in progress; [data] still represents the previously
     * loaded items.
     */
    public data object Refreshing : State<Nothing>

    /**
     * The producer is idle and [data] holds the items observed so far.
     */
    public data object Idle : State<Nothing>
}

/**
 * `true` when the producer is idle and a refresh may be triggered.
 */
public inline val Paginatable<*, *>.isRefreshable: Boolean
    get() = isIdle

/**
 * `true` when [index] points to the last loaded item and more pages are available — useful
 * for trigger-on-scroll patterns.
 */
public fun Paginatable<*, *>.canLoadNextForIndex(
    index: Int,
): Boolean = hasMore && index == data.lastIndex

/**
 * `true` when [item] is the last loaded item and more pages are available.
 */
public fun <T : Any> Paginatable<T, *>.canLoadNextForItem(
    item: T?,
): Boolean = hasMore && item == data.lastOrNull()

/**
 * `true` when the [Paginatable] is performing the initial fetch.
 */
public inline val Paginatable<*, *>.isLoading: Boolean
    get() = state == Paginatable.Loading

/**
 * `true` when the [Paginatable] is fetching an additional page.
 */
public inline val Paginatable<*, *>.isLoadingNext: Boolean
    get() = state == Paginatable.LoadingNext

/**
 * `true` when the [Paginatable] is performing a full re-fetch.
 */
public inline val Paginatable<*, *>.isRefreshing: Boolean
    get() = state == Paginatable.Refreshing

/**
 * `true` when the producer is idle (including the terminal [Paginatable.Exception] state) and
 * may accept new requests.
 */
public inline val Paginatable<*, *>.isIdle: Boolean
    get() = state == Paginatable.Idle || isException

/**
 * `true` when the [Paginatable] is in the terminal [Paginatable.Exception] state.
 */
public inline val Paginatable<*, *>.isException: Boolean
    get() = state is Paginatable.Exception<*>

/**
 * Returns a copy of this [Paginatable] whose [Paginatable.data] is the result of applying
 * [f] to the current list.
 */
public inline fun <T : Any, Err> Paginatable<T, Err>.data(
    f: PersistentList<T>.() -> PersistentList<T>,
): Paginatable<T, Err> = copy(data = f(data))

/**
 * Transitions the [Paginatable] to [Paginatable.LoadingNext].
 *
 * @throws IllegalArgumentException if [Paginatable.data] is empty; in that case a full reload
 *   (i.e. [toLoading]) must be requested instead.
 */
public fun <T : Any, Err> Paginatable<T, Err>.toLoadingNext(): Paginatable<T, Err> {
    checkCanLoadNextPage()
    return copy(state = Paginatable.LoadingNext)
}

/**
 * Returns a copy of this [Paginatable] in the [Paginatable.Loading] state with an empty
 * payload. Use this when restarting from page zero.
 */
public fun <T : Any, Err> Paginatable<T, Err>.toLoading(): Paginatable<T, Err> =
    copy(state = Paginatable.Loading, data = persistentListOf())

/**
 * Returns a copy of this [Paginatable] in the [Paginatable.Refreshing] state, preserving the
 * existing items.
 */
public fun <T : Any, Err> Paginatable<T, Err>.toRefreshing(): Paginatable<T, Err> =
    copy(state = Paginatable.Refreshing)

/**
 * Returns a copy of this [Paginatable] in the [Paginatable.Idle] state, preserving existing
 * items and [Paginatable.hasMore].
 */
public fun <T : Any, Err> Paginatable<T, Err>.toIdle(): Paginatable<T, Err> =
    copy(state = Paginatable.Idle)

/**
 * Returns a copy of this [Paginatable] in the [Paginatable.Idle] state after applying [page].
 *
 * Behaviour depends on the current [Paginatable.state]:
 * - From [Paginatable.LoadingNext] or [Paginatable.Exception] the page is **appended** to the
 *   existing items.
 * - From [Paginatable.Loading], [Paginatable.Refreshing] or [Paginatable.Idle] the page
 *   **replaces** the existing items.
 *
 * In every case [Paginatable.hasMore] is updated from [Page.hasMore].
 */
public fun <T : Any, Err> Paginatable<T, Err>.toIdle(
    page: Page<T>,
): Paginatable<T, Err> =
    when (state) {
        Paginatable.LoadingNext, is Paginatable.Exception<*> -> copy(
            data = data.addAll(page.data),
            state = Paginatable.Idle,
            hasMore = page.hasMore,
        )

        Paginatable.Loading, Paginatable.Refreshing, Paginatable.Idle -> copy(
            data = page.data.toPersistentList(),
            state = Paginatable.Idle,
            hasMore = page.hasMore,
        )
    }

/**
 * Returns a copy of this [Paginatable] in the terminal [Paginatable.Exception] state wrapping
 * [error].
 */
public fun <T : Any, Err> Paginatable<T, Err>.toException(
    error: Err,
): Paginatable<T, Err> = copy(state = Paginatable.Exception(error))

private fun Paginatable<*, *>.checkCanLoadNextPage() {
    require(data.isNotEmpty()) {
        "$this doesn't contain items and thus LoadingNext transition is prohibited. " +
            "Request full reload instead"
    }
}
