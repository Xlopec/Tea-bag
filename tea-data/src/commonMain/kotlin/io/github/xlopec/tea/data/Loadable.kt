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
import kotlin.jvm.JvmName

/**
 * Container that wraps an arbitrary value together with the [State] of the operation that
 * produces it.
 *
 * A `Loadable` is the unit of state used when a screen or feature renders data sourced
 * asynchronously (e.g. from a network call or a database query). Use the companion factories
 * to construct the initial value and the `to…` extensions to transition between states as the
 * underlying request progresses.
 *
 * For paginated collections see [Paginatable].
 *
 * @param T type of the wrapped data
 * @property data value the producer has emitted so far; the meaning depends on [state]
 * @property state lifecycle of the producer
 */
public data class Loadable<out T> internal constructor(
    val data: T,
    val state: State,
) {
    public companion object {

        /**
         * Creates an [Idle] [Loadable] holding [data] — typically used as the initial value of
         * a feature that already knows its starting payload.
         */
        public fun <T> idleSingle(
            data: T,
        ): Loadable<T> = Loadable(data = data, state = Idle)

        /**
         * Creates a [Loading] [Loadable] for an optional single value initialised to `null`.
         */
        public fun <T> loadingSingle(): Loadable<T?> = Loadable(data = null, state = Loading)

        /**
         * Creates a [Loading] [Loadable] that already carries a non-`null` placeholder value.
         */
        public fun <T> loadingSingle(
            data: T,
        ): Loadable<T> = Loadable(data = data, state = Loading)

        /**
         * Creates a [Loading] [Loadable] holding an empty [PersistentList] of [T].
         */
        public fun <T : Any> loadingList(): Loadable<PersistentList<T>> =
            Loadable(data = persistentListOf(), state = Loading)

        /**
         * Creates an [Idle] [Loadable] holding a (possibly empty) [PersistentList] of [T].
         */
        public fun <T : Any> idleList(
            data: PersistentList<T> = persistentListOf(),
        ): Loadable<PersistentList<T>> = Loadable(data = data, state = Idle)
    }

    /**
     * Lifecycle of the producer backing a [Loadable].
     */
    public sealed interface State

    /**
     * Terminal failure state.
     *
     * @property exception cause of the failure
     */
    public data class Exception internal constructor(
        val exception: Throwable,
    ) : State

    /**
     * Initial load is in progress; [data] should be considered a placeholder.
     */
    public data object Loading : State

    /**
     * A reload of an already-loaded [data] is in progress; [data] still represents the
     * previously observed value.
     */
    public data object Refreshing : State

    /**
     * The producer is idle and [data] holds the latest observed value.
     */
    public data object Idle : State
}

/**
 * `true` when the [Loadable] is in a state from which a refresh may be triggered.
 */
public val Loadable<*>.isRefreshable: Boolean
    get() = isIdle

/**
 * `true` when the [Loadable] is performing an initial load.
 */
public val Loadable<*>.isLoading: Boolean
    get() = state === Loadable.Loading

/**
 * `true` when the [Loadable] is performing a refresh of an already-loaded value.
 */
public val Loadable<*>.isRefreshing: Boolean
    get() = state === Loadable.Refreshing

/**
 * `true` when the producer is idle (including the terminal [Loadable.Exception] state) and
 * may accept new requests.
 */
public val Loadable<*>.isIdle: Boolean
    get() = state === Loadable.Idle || isException

/**
 * `true` when the [Loadable] is in the terminal [Loadable.Exception] state.
 */
public val Loadable<*>.isException: Boolean
    get() = state is Loadable.Exception

/**
 * Returns a copy of this [Loadable] whose [Loadable.data] is the result of applying [how] to
 * the current list.
 */
@JvmName("dataList")
public fun <T> Loadable<PersistentList<T>>.dataList(
    how: PersistentList<T>.() -> PersistentList<T>,
): Loadable<PersistentList<T>> = copy(data = how(data))

/**
 * Returns a copy of this [Loadable] whose [Loadable.data] is the result of applying [how] to
 * the current value.
 */
@JvmName("dataSingle")
public fun <T> Loadable<T>.data(
    how: T.() -> T,
): Loadable<T> = copy(data = how(data))

/**
 * Returns a copy of this list-typed [Loadable] in the [Loadable.Loading] state with an empty
 * payload. Use this when restarting a load from scratch.
 */
@JvmName("toLoadingList")
public fun <T> Loadable<PersistentList<T>>.toLoading(): Loadable<PersistentList<T>> =
    copy(state = Loadable.Loading, data = persistentListOf())

/**
 * Returns a copy of this nullable single-value [Loadable] in the [Loadable.Loading] state
 * with the value cleared to `null`.
 */
@JvmName("toLoadingSingle")
public fun <T> Loadable<T?>.toLoading(): Loadable<T?> =
    copy(state = Loadable.Loading, data = null)

/**
 * Returns a copy of this [Loadable] in the [Loadable.Refreshing] state, preserving the
 * existing [Loadable.data].
 */
public fun <T> Loadable<T>.toRefreshing(): Loadable<T> =
    copy(state = Loadable.Refreshing)

/**
 * Returns a copy of this [Loadable] in the [Loadable.Refreshing] state, applying [updater]
 * to the existing [Loadable.data] before storing it.
 */
public fun <T> Loadable<T>.toRefreshing(
    updater: T.() -> T = { this },
): Loadable<T> =
    copy(state = Loadable.Refreshing, data = updater(data))

/**
 * Returns a copy of this [Loadable] in the [Loadable.Idle] state, preserving the existing
 * [Loadable.data].
 */
@JvmName("toIdleSingle")
public fun <T> Loadable<T>.toIdle(): Loadable<T> = copy(state = Loadable.Idle)

/**
 * Returns a copy of this [Loadable] in the [Loadable.Idle] state with [data] as the new
 * payload.
 */
@JvmName("toIdleSingleWithData")
public fun <T> Loadable<T>.toIdle(
    data: T,
): Loadable<T> = copy(
    data = data,
    state = Loadable.Idle,
)

/**
 * Returns a copy of this list-typed [Loadable] in the [Loadable.Idle] state, preserving the
 * existing items.
 */
@JvmName("toIdleList")
public fun <T> Loadable<PersistentList<T>>.toIdle(): Loadable<PersistentList<T>> =
    copy(state = Loadable.Idle)

/**
 * Returns a copy of this list-typed [Loadable] in the [Loadable.Idle] state with [loaded] as
 * the new payload.
 */
@JvmName("toIdleListWithData")
public fun <T> Loadable<PersistentList<T>>.toIdle(
    loaded: PersistentList<T>,
): Loadable<PersistentList<T>> = copy(
    data = loaded,
    state = Loadable.Idle,
)

/**
 * Returns a copy of this list-typed [Loadable] in the terminal [Loadable.Exception] state
 * wrapping [cause].
 */
@JvmName("toExceptionList")
public fun <T> Loadable<PersistentList<T>>.toException(
    cause: Throwable,
): Loadable<PersistentList<T>> = copy(state = Loadable.Exception(cause))

/**
 * Returns a copy of this [Loadable] in the terminal [Loadable.Exception] state wrapping
 * [cause].
 */
@JvmName("toExceptionSingle")
public fun <T> Loadable<T>.toException(
    cause: Throwable,
): Loadable<T> = copy(state = Loadable.Exception(cause))
