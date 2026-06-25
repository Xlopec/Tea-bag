/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

import arrow.core.Either
import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.Log
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.feature.network.ArticleElement
import io.github.xlopec.reader.app.feature.network.ArticleResponse
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import io.github.xlopec.reader.app.misc.mapToPersistentList
import io.github.xlopec.tea.data.Page
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.FilterType.Favorite
import io.github.xlopec.reader.app.model.FilterType.Regular
import io.github.xlopec.reader.app.model.FilterType.Trending
import io.github.xlopec.tea.core.Sink
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.core.sideEffect
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public fun interface ShareArticle {
    public fun share(
        article: Article,
    )
}

context(_: Sink<Message>, _: CoroutineScope)
public fun <Env> Env.resolveForArticles(
    command: ArticlesCommand,
) where Env : NewsApi,
        Env : LocalStorage,
        Env : ShareArticle {
    when (command) {
        is DoLoadArticles -> loadArticles(command)
        is DoSaveArticle -> storeArticle(command.article)
        is DoRemoveArticle -> removeArticle(command.article)
        is DoShareArticle -> command.sideEffect(Dispatchers.Main) {
            share(article)
        }

        is DoStoreFilter -> command sideEffect { storeFilter(filter) }
        is DoLoadFilter -> command effect { FilterLoaded(id, findFilter(type)) }
    }
}

context(_: Sink<Message>, _: CoroutineScope)
private fun <Env> Env.loadArticles(
    command: DoLoadArticles,
) where Env : LocalStorage, Env : NewsApi {
    val paging = command.paging
    val (type, input, sources) = command.filter

    command effects {
        when (type) {
            Regular -> fetchFromEverything(input, sources, paging).toArticlesMessage()
            Favorite -> setOf(ArticlesLoaded(id, findAllArticles(filter)))
            Trending -> fetchTopHeadlines(input, sources, paging).toArticlesMessage()
        }
    }
}

context(_: Sink<Message>, _: CoroutineScope)
private fun LocalStorage.storeArticle(
    article: Article,
) = effect {
    insertArticle(article)
    ArticleUpdated(article)
}

context(_: Sink<Message>, _: CoroutineScope)
private fun LocalStorage.removeArticle(
    article: Article,
) = effect {
    deleteArticle(article.url)
    ArticleUpdated(article)
}

context(storage: LocalStorage, command: DoLoadArticles)
private suspend fun Either<AppException, ArticleResponse>.toArticlesMessage() = fold(
    { setOf(ArticlesLoadException(command.id, it), Log(it, command, command.id)) },
    { setOf(ArticlesLoaded(command.id, storage.toPage(it, command.paging))) },
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
    val tail = if (skip == 0 || results.isEmpty()) {
        results
    } else {
        results.subList(skip, results.size)
    }

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
