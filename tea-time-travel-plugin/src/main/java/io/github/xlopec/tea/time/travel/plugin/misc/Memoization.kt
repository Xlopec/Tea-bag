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

// todo implement empty static instance with a factory method
data class Key7(
    val a1: Any?,
    val a2: Any? = null,
    val a3: Any? = null,
    val a4: Any? = null,
    val a5: Any? = null,
    val a6: Any? = null,
    val a7: Any? = null
)

inline fun <A1, A2, R> memoize(
    capacity: UInt = 10U,
    crossinline f: (A1, A2) -> R
) =
    object : (A1, A2) -> R {

        private val cache = LruCache<Key7, R>(capacity)

        override fun invoke(
            p1: A1,
            p2: A2
        ): R =
            cache.getOrPut(Key7(p1, p2)) { f(p1, p2) }
    }
