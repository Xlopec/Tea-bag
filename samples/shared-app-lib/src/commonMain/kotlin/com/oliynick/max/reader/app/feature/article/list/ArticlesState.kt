/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.TabScreen
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.misc.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
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

data class ScrollState(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
) {
    companion object {
        val Initial = ScrollState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)
    }
}

typealias ArticlesLoadable = Loadable<PersistentList<Article>>

data class ArticlesState(
    override val id: ScreenId,
    val filter: Filter,
    val loadable: ArticlesLoadable,
    val scrollState: ScrollState = ScrollState.Initial,
) : TabScreen {

    companion object {

        const val ArticlesPerPage = 10

        fun newLoading(
            id: ScreenId,
            filter: Filter,
            articles: PersistentList<Article> = persistentListOf(),
        ) = ArticlesState(id, filter, Loadable.newLoading(articles))
    }

}

fun ArticlesState.toLoadingNext() =
    copy(loadable = loadable.toLoadingNext())

fun ArticlesState.toLoading(
    filter: Filter = this.filter
) = copy(filter = filter, loadable = loadable.toLoading())

fun ArticlesState.toRefreshing() =
    copy(loadable = loadable.toRefreshing())

fun ArticlesState.toPreview(
    page: Page<Article>,
): ArticlesState = copy(loadable = loadable.toPreview(page))

fun ArticlesState.toException(
    cause: AppException,
) = copy(loadable = loadable.toException(cause))

fun ArticlesState.updateArticle(
    new: Article,
): ArticlesState =
    copy(loadable = loadable.updated { articles -> articles.replace(new) { it.url == new.url } })

fun ArticlesState.prependArticle(
    new: Article,
): ArticlesState = copy(loadable = loadable.updated { it.add(0, new) })

fun ArticlesState.removeArticle(
    victim: Article,
): ArticlesState =
    copy(loadable = loadable.updated { articles -> articles.remove { it.url == victim.url } })
