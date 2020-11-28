package com.max.reader.screens.article.list

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenState
import com.max.reader.domain.Article

sealed class ArticlesState : ScreenState() {
    abstract val query: Query
}

enum class QueryType {
    Regular, Favorite, Trending
}

data class Query(
    val input: String,
    val type: QueryType
)

data class ArticlesLoadingState(
    override val id: ScreenId,
    override val query: Query
) : ArticlesState()

data class ArticlesPreviewState(
    override val id: ScreenId,
    override val query: Query,
    val articles: List<Article>
) : ArticlesState()

data class ArticlesErrorState(
    override val id: ScreenId,
    override val query: Query,
    val cause: Throwable
) : ArticlesState()

// todo replace with immutable collection
fun ArticlesPreviewState.updateArticle(
    new: Article
): ArticlesPreviewState = copy(articles = articles.map { if (it.url == new.url) new else it })

fun ArticlesPreviewState.prependArticle(
    new: Article
): ArticlesPreviewState = copy(articles = listOf(new) + articles)

fun ArticlesPreviewState.removeArticle(
    victim: Article
): ArticlesPreviewState = copy(articles = articles.filter { it.url != victim.url })
