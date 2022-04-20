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

/**
 * The most simple, non-synchronized version of LRU cache
 */
class LruCache<K, V>(val capacity: UInt) {

    init {
        require(capacity > 0U) { "Capacity should be greater than 0" }
    }

    // [most used, ..., least used, null, ..., null]
    private val cache by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<Pair<K, V>?>(capacity.toInt()) }
    private var realSize = 0U

    val size: UInt
        get() = realSize

    val isEmpty: Boolean
        inline get() = size == 0U

    // todo add checks
    fun getOrPut(
        k: K,
        v: () -> V
    ): V {

        for (i in cache.indices) {

            val cached = cache[i] ?: break

            if (cached.first == k) {
                moveToFront(cached, i)
                return cached.second
            }
        }

        val newCached = k to v()

        moveToFront(newCached, cache.lastIndex)

        if (realSize < capacity) {
            realSize++
        }

        return newCached.second
    }

    private fun moveToFront(
        elem: Pair<K, V>,
        i: Int
    ) {

        for (j in i downTo 1) {
            cache[j] = cache[j - 1]
        }

        cache[0] = elem
    }

}
