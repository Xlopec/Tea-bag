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
import com.oliynick.max.reader.app.feature.article.list.ArticlesState.TransientState.*
import com.oliynick.max.reader.domain.Article

enum class QueryType {
    Regular, Favorite, Trending
}

data class Query(
    val input: String,
    val type: QueryType,
)

data class ArticlesState(
    override val id: ScreenId,
    val query: Query,
    val articles: List<Article>,
    val hasMoreArticles: Boolean,
    val transientState: TransientState,
) : TabScreen {

    sealed class TransientState {
        data class Exception(
            val th: AppException,
        ) : TransientState()

        object Loading : TransientState()
        object LoadingNext : TransientState()
        object Refreshing : TransientState()
        object Preview : TransientState()
    }

    val isLoading = transientState === Loading

    val isLoadingNext = transientState === LoadingNext

    val isRefreshing = transientState === Refreshing

    val isPreview = transientState === Preview

    companion object {

        const val ArticlesPerPage = 10

        fun newLoading(
            id: ScreenId,
            query: Query,
            articles: List<Article> = emptyList(),
        ) = ArticlesState(id, query, articles, false, Loading)
    }

}

// todo replace with immutable collection
fun ArticlesState.toLoadingNext() =
    copy(transientState = LoadingNext)

fun ArticlesState.toLoading() =
    copy(transientState = Loading)

fun ArticlesState.toRefreshing() =
    copy(transientState = Refreshing)

fun ArticlesState.toPreview(
    page: Page,
): ArticlesState =
    when (transientState) {
        LoadingNext, is Exception -> {
            copy(
                articles = articles + page.articles,
                transientState = Preview,
                hasMoreArticles = page.hasMore
            )
        }
        Loading, Refreshing -> copy(
            articles = page.articles,
            transientState = Preview,
            hasMoreArticles = page.hasMore
        )
        Preview -> this
    }

fun ArticlesState.toException(
    cause: AppException,
) = copy(transientState = Exception(cause))

fun ArticlesState.updateArticle(
    new: Article,
): ArticlesState = copy(articles = articles.map { if (it.url == new.url) new else it })

fun ArticlesState.prependArticle(
    new: Article,
): ArticlesState = copy(articles = listOf(new) + articles)

fun ArticlesState.removeArticle(
    victim: Article,
): ArticlesState = copy(articles = articles.filter { it.url != victim.url })
