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

package io.github.xlopec.reader.app.feature.article.list

import io.github.xlopec.reader.app.feature.article.list.ArticlesState.Companion.ArticlesPerPage
import kotlinx.collections.immutable.ImmutableList

public data class Page<out T>(
    val data: ImmutableList<T>,
    val hasMore: Boolean = false
)

public data class Paging(
    val currentSize: Int,
    val resultsPerPage: Int = ArticlesPerPage
) {
    internal companion object {
        val FirstPage = Paging(currentSize = 0)
    }
}

/**
 * Calculates and returns next page for current Paging instance.
 * For 0 page it'll return 1, which is acceptable by API
 */
internal inline val Paging.nextPage: Int
    get() = (currentSize / resultsPerPage) + 1

internal fun ArticlesState.nextPage(
    resultsPerPage: Int = ArticlesPerPage
) = Paging(loadable.data.size, resultsPerPage)
