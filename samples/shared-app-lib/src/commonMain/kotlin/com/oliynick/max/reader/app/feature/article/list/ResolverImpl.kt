@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.entities.shared.datatypes.Either
import com.oliynick.max.entities.shared.datatypes.fold
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.ScreenMessage
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.feature.article.list.FilterType.*
import com.oliynick.max.reader.app.feature.network.ArticleElement
import com.oliynick.max.reader.app.feature.network.ArticleResponse
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.reader.app.misc.mapToPersistentList
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.collections.immutable.ImmutableList

fun interface ShareArticle {
    fun share(
        article: Article,
    )
}

fun <Env> ArticlesResolver(
    shareDelegate: ShareArticle,
): ArticlesResolver<Env> where Env : NewsApi, Env : LocalStorage =
    ArticlesResolverImpl(shareDelegate)

class ArticlesResolverImpl<Env>(
    private val shareDelegate: ShareArticle,
) : ArticlesResolver<Env> where Env : NewsApi, Env : LocalStorage {
    override suspend fun Env.resolve(
        command: ArticlesCommand,
    ): Set<Message> =
        when (command) {
            is DoLoadArticles -> loadArticles(command)
            is DoSaveArticle -> storeArticle(command.article)
            is DoRemoveArticle -> removeArticle(command.article)
            is DoShareArticle -> sideEffect { shareDelegate.share(command.article) }
            is DoStoreFilter -> command sideEffect { storeFilter(filter) }
            is DoLoadFilter -> command effect { FilterLoaded(id, findFilter(type)) }
        }
}

private suspend fun <Env> Env.loadArticles(
    command: DoLoadArticles,
): Set<ArticlesMessage> where Env : LocalStorage, Env : NewsApi =
    command effect {

        val (type, input, sources) = filter

        when (type) {
            Regular -> toArticlesMessage(fetchFromEverything(input, sources, paging), command)
            Favorite -> ArticlesLoaded(command.id, findAllArticles(filter))
            Trending -> toArticlesMessage(fetchTopHeadlines(input, sources, paging), command)
        }
    }

private suspend fun LocalStorage.storeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    insertArticle(article)
    ArticleUpdated(article)
}

private suspend fun LocalStorage.removeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    deleteArticle(article.url)
    ArticleUpdated(article)
}

private suspend fun LocalStorage.toArticlesMessage(
    either: Either<ArticleResponse, AppException>,
    command: DoLoadArticles,
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
): Page<Article> {
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
): ImmutableList<Article> =
    articles.mapToPersistentList { elem -> toArticle(elem) }

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
