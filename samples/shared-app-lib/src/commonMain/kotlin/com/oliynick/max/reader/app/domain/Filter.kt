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

package com.oliynick.max.reader.app.domain

import com.oliynick.max.reader.app.misc.coerceIn
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlin.jvm.JvmInline

enum class FilterType {
    Regular, Favorite, Trending
}

/**
 * Represents user query, never empty
 */
@JvmInline
value class Query private constructor(
    val value: String
) {
    companion object {

        private const val MaxQueryLength = 500U
        private const val MinQueryLength = 1U

        fun of(
            input: String?
        ) = input?.coerceIn(MinQueryLength, MaxQueryLength)?.replace("\n", "")?.let(::Query)
    }
}

data class Filter(
    val type: FilterType,
    val query: Query? = null,
    val sources: PersistentSet<SourceId> = persistentSetOf(),
) {
    companion object {
        /**
         * API doesn't accept more than 20 sources per request
         */
        const val StoreSourcesLimit = 20U
    }
}
