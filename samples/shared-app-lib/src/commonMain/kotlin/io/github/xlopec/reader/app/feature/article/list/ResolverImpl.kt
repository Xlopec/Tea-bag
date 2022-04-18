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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app.feature.article.list

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.Log
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.ScreenMessage
import io.github.xlopec.reader.app.domain.Article
import io.github.xlopec.reader.app.domain.FilterType.*
import io.github.xlopec.reader.app.feature.network.ArticleElement
import io.github.xlopec.reader.app.feature.network.ArticleResponse
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import io.github.xlopec.reader.app.misc.mapToPersistentList
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.data.fold
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
            is DoShareArticle -> { shareDelegate.share(command.article); emptySet() }
            is DoStoreFilter -> { storeFilter(command.filter); emptySet() }
            is DoLoadFilter -> command effect { FilterLoaded(id, findFilter(type)) }
        }
}

private suspend fun <Env> Env.loadArticles(
    command: DoLoadArticles,
): Set<Message> where Env : LocalStorage, Env : NewsApi {

    val paging = command.paging
    val (type, input, sources) = command.filter

    return when (type) {
        Regular -> toArticlesMessage(fetchFromEverything(input, sources, paging), command)
        Favorite -> setOf(ArticlesLoaded(command.id, findAllArticles(command.filter)))
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
) = either.fold(
    left = { setOf(ArticlesLoaded(command.id, toPage(it, command.paging))) },
    right = { setOf(ArticlesOperationException(command.id, it), Log(it, command, command.id)) }
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
    published = element.publishedAt,
    source = element.source.id
)
