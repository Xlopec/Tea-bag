package com.max.reader.screens.article.list

import com.max.reader.app.ScreenState
import com.max.reader.app.ScreenId
import com.max.reader.domain.Article

sealed class ArticlesState : ScreenState() {
    abstract val criteria: LoadCriteria
}

sealed class LoadCriteria {

    data class Query(
        val query: String
    ) : LoadCriteria()

    object Favorite : LoadCriteria()

    object Trending : LoadCriteria()
}

data class ArticlesLoadingState(
    override val id: ScreenId,
    override val criteria: LoadCriteria
) : ArticlesState()

data class ArticlesPreviewState(
    override val id: ScreenId,
    override val criteria: LoadCriteria,
    val articles: List<Article>
) : ArticlesState()

data class ArticlesLoadingError(
    override val id: ScreenId,
    override val criteria: LoadCriteria,
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
