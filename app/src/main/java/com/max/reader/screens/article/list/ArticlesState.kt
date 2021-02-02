@file:Suppress("FunctionName")

package com.max.reader.screens.article.list

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenState
import com.max.reader.domain.Article
import com.max.reader.screens.article.list.ArticlesState.TransientState.*

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
) : ScreenState() {

    sealed class TransientState {
        data class Exception(val th: Throwable) : TransientState()
        object Loading : TransientState()
        object Refreshing : TransientState()
        object Preview : TransientState()
    }

    val isLoading = transientState === Loading

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
    copy(transientState = Loading)

fun ArticlesState.toRefreshing() =
    copy(transientState = Refreshing)

fun ArticlesState.toPreview(
    append: List<Article>,
    hasMore: Boolean,
): ArticlesState =
    when (transientState) {
        Loading, is Exception -> {
            copy(
                articles = articles + append,
                transientState = Preview,
                hasMoreArticles = hasMore
            )
        }
        Refreshing -> copy(
            articles = append,
            transientState = Preview,
            hasMoreArticles = hasMore
        )
        Preview -> this
    }

fun ArticlesState.toException(
    cause: Throwable,
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
