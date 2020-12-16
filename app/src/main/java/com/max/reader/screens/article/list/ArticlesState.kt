@file:Suppress("FunctionName")

package com.max.reader.screens.article.list

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenState
import com.max.reader.domain.Article
import com.max.reader.misc.E0
import com.max.reader.misc.E1
import com.max.reader.misc.Either2

enum class QueryType {
    Regular, Favorite, Trending
}

data class Query(
    val input: String,
    val type: QueryType
)

data class ArticlesState(
    override val id: ScreenId,
    val query: Query,
    val articles: List<Article>,
    val transientState: Either2<Throwable, Boolean>
) : ScreenState() {

    val isLoading = (transientState as? E1)?.r == true

    val isPreview = !isLoading

    companion object {

        fun loading(
            id: ScreenId,
            query: Query,
            articles: List<Article> = emptyList()
        ) = ArticlesState(id, query, articles, E1(true))

        fun preview(
            id: ScreenId,
            query: Query,
            articles: List<Article> = emptyList()
        ) = ArticlesState(id, query, articles, E1(false))

        fun exception(
            id: ScreenId,
            query: Query,
            cause: Throwable,
            articles: List<Article> = emptyList()
        ) = ArticlesState(id, query, articles, E0(cause))
    }

}

// todo replace with immutable collection
fun ArticlesState.updateArticle(
    new: Article
): ArticlesState = copy(articles = articles.map { if (it.url == new.url) new else it })

fun ArticlesState.prependArticle(
    new: Article
): ArticlesState = copy(articles = listOf(new) + articles)

fun ArticlesState.removeArticle(
    victim: Article
): ArticlesState = copy(articles = articles.filter { it.url != victim.url })
