/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xlopec.tea.time.travel.plugin.misc

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

interface DiffCallback<in T1, in T2> {

    fun areItemsTheSame(
        oldItem: T1,
        newItem: T2
    ): Boolean

    fun areContentsTheSame(
        oldItem: T1,
        newItem: T2
    ): Boolean

}

interface UpdateCallback<in T1, in T2> {

    fun onContentUpdated(
        oldItem: T1,
        oldIndex: Int,
        newItem: T2,
        newIndex: Int
    ) = Unit

    fun onItemInserted(
        item: T1,
        index: Int
    ) = Unit

    fun onItemRemoved(
        item: T1,
        index: Int
    ) = Unit

}

/**
 * Replaces content in receiver list with content of replace list.
 * Changes in target list calculated using Eugene W. Myers diff algorithm.
 *
 * Might produce uncaught exceptions and poorly tested, so be careful.
 * Complexity is O(N + M)
 */
// todo add batch updates and decision strategy
inline fun <L : MutableList<T1>, T1, T2> L.replaceAll(
    replaceWith: List<T2>,
    diffCallback: DiffCallback<T1, T2>,
    update: UpdateCallback<T1, T2>? = null,
    crossinline supply: (T2) -> T1
): L {

    var x = 0
    var y = 0

    var moodX = size
    val moodY = replaceWith.size

    while (x < size || y < replaceWith.size) {

        require(moodX == size) { "Receiver collection shouldn't be modified during modification" }
        require(moodY == replaceWith.size) { "Replace collection shouldn't be modified during modification" }

        while (x < size && y < replaceWith.size && diffCallback.areItemsTheSame(this[x], replaceWith[y])) {
            // move towards diagonal
            val old = this[x]
            val new = replaceWith[y]

            if (!diffCallback.areContentsTheSame(old, new)) {
                this[x] = supply(new)
                update?.onContentUpdated(old, x, new, y)
            }

            x += 1
            y += 1
        }

        while (x < size && (y == replaceWith.size || (y < replaceWith.size && !diffCallback.areItemsTheSame(this[x], replaceWith[y])))) {
            // move down
            val old = removeAt(x)
            update?.onItemRemoved(old, x)
            moodX -= 1
        }

        while (y < replaceWith.size && (x >= size || !diffCallback.areItemsTheSame(this[x], replaceWith[y]))) {
            // move right
            val new = supply(replaceWith[y])
            add(x, new)
            update?.onItemInserted(new, x)

            x += 1
            y += 1
            moodX += 1
        }
    }

    return this
}

fun <L : MutableList<T>, T> L.replaceAll(
    replaceWith: List<T>,
    diffCallback: DiffCallback<T, T>,
    update: UpdateCallback<T, T>? = null
): L {
    return replaceAll(replaceWith, diffCallback, update, ::identity)
}

fun <E> List<E>.mergeWith(
    with: Collection<E>
): List<E> {
    val merged = ArrayList<E>(this)

    for (e in with) {
        if (e !in merged) {
            merged += e
        }
    }

    merged.trimToSize()
    return merged
}

private fun <T> identity(t: T): T = t

fun <E> setOfNonNull(
    vararg elements: E?
): Set<E> {
    val set = HashSet<E>(elements.size)

    for (e in elements) {
        if (e != null) {
            set += e
        }
    }

    return set
}

inline fun <E, M : MutableList<E>> M.mapInPlace(
    how: (E) -> E
): M {

    for (i in indices) {
        this[i] = how(this[i])
    }

    return this
}

inline fun <E, R> PersistentList<E>.mapNotNull(
    how: (E) -> R?
): PersistentList<R> =
    persistentListOf<R>()
        .builder()
        .also { res -> forEach { o -> val t = how(o); if (t != null) res.add(t) } }
        .build()

inline fun <E, R> PersistentList<E>.map(
    how: (E) -> R
): PersistentList<R> =
    persistentListOf<R>()
        .builder()
        .also { res -> forEach { o -> res.add(how(o)) } }
        .build()
