@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.app.datatypes.Either
import com.oliynick.max.reader.app.datatypes.fold
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.network.ArticleElement
import com.oliynick.max.reader.network.ArticleResponse
import com.oliynick.max.reader.network.Page
import com.oliynick.max.tea.core.component.effect

internal suspend fun <Env> Env.loadArticles(
    command: LoadArticlesByQuery
): Set<ArticlesMessage> where Env : LocalStorage, Env : NewsApi =
    command.effect {

        val (input, type) = query

        when (type) {
            Regular -> toArticlesMessage(fetchFromEverything(input, currentSize, resultsPerPage), command)
            Favorite -> ArticlesLoaded(command.id, findAllArticles(input))
            Trending -> toArticlesMessage(fetchTopHeadlines(input, currentSize, resultsPerPage), command)
        }
    }

internal suspend fun LocalStorage.storeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    insertArticle(article)
    ArticleUpdated(article)
}

internal suspend fun LocalStorage.removeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    deleteArticle(article.url)
    ArticleUpdated(article)
}

private suspend fun LocalStorage.toArticlesMessage(
    either: Either<ArticleResponse, AppException>,
    command: LoadArticlesByQuery
) =
    either.fold(
        left = { response ->
            ArticlesLoaded(
                command.id,
                toPage(response, command.currentSize, command.resultsPerPage)
            )
        },
        right = { th -> ArticlesOperationException(command.id, th) }
    )

private fun ArticlesLoaded(
    screenId: ScreenId,
    page: Page
): ArticlesLoaded {
    val (articles, hasMore) = page
    return ArticlesLoaded(screenId, articles, hasMore)
}

private suspend fun LocalStorage.toPage(
    response: ArticleResponse,
    currentSize: Int,
    resultsPerPage: Int,
): Page {
    val (total, results) = response
    val skip = currentSize % resultsPerPage

    val tail = if (skip == 0 || results.isEmpty()) results
    else results.subList(skip, results.size)

    return Page(toArticles(tail), currentSize + tail.size < total)
}

private suspend fun LocalStorage.toArticles(
    articles: Iterable<ArticleElement>,
) = articles.map { elem -> toArticle(elem) }

private suspend fun LocalStorage.toArticle(
    element: ArticleElement,
) = Article(
        url = element.url,
        title = element.title,
        author = element.author,
        description = element.description,
        urlToImage = element.urlToImage,
        isFavorite = isFavoriteArticle(element.url),
        published = element.publishedAt
    )