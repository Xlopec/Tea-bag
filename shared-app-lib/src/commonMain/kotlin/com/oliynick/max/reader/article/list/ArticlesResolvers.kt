@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.command.LoadArticlesByQuery
import com.oliynick.max.reader.app.datatypes.Either
import com.oliynick.max.reader.app.datatypes.fold
import com.oliynick.max.reader.app.message.ScreenMessage
import com.oliynick.max.reader.app.storage.LocalStorage
import com.oliynick.max.reader.article.list.QueryType.Favorite
import com.oliynick.max.reader.article.list.QueryType.Regular
import com.oliynick.max.reader.article.list.QueryType.Trending
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.network.ArticleElement
import com.oliynick.max.reader.network.ArticleResponse
import com.oliynick.max.tea.core.component.effect

internal suspend fun <Env> Env.loadArticles(
    command: LoadArticlesByQuery
): Set<ArticlesMessage> where Env : LocalStorage, Env : NewsApi =
    command.effect {

        val (input, type) = query

        when (type) {
            Regular -> toArticlesMessage(fetchFromEverything(input, paging), command)
            Favorite -> ArticlesLoaded(command.id, findAllArticles(input))
            Trending -> toArticlesMessage(fetchTopHeadlines(input, paging), command)
        }
    }

internal suspend fun LocalStorage.storeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    insertArticle(article)
    OnArticleUpdated(article)
}

internal suspend fun LocalStorage.removeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    deleteArticle(article.url)
    OnArticleUpdated(article)
}

private suspend fun LocalStorage.toArticlesMessage(
    either: Either<ArticleResponse, AppException>,
    command: LoadArticlesByQuery
) =
    either.fold(
        left = { response ->
            ArticlesLoaded(
                command.id,
                toPage(response, command.paging)
            )
        },
        right = { th -> ArticlesOperationException(command.id, th) }
    )

private suspend fun LocalStorage.toPage(
    response: ArticleResponse,
    paging: Paging,
): Page {
    val (currentSize, resultsPerPage) = paging
    val (total, results) = response
    val skip = currentSize % resultsPerPage
    // removes data duplicates by skipping and removing
    // overlapping data indices
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