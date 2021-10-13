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

package com.oliynick.max.reader.article.list

import android.app.Application
import android.content.Intent
import android.content.Intent.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.network.Page
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect
import io.ktor.client.features.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

fun <Env> ArticlesResolver(
    gson: Gson,
    application: Application,
): ArticlesResolver<Env> where Env : NewsApi, Env : LocalStorage = object : ArticlesResolver<Env> {
        override suspend fun Env.resolve(command: ArticlesCommand): Set<Message> {
            suspend fun resolve() =
                when (command) {
                    is LoadArticlesByQuery -> fetch(this, this, command)
                    is SaveArticle -> store(command.article)
                    is RemoveArticle -> remove(command.article)
                    is DoShareArticle -> application.shareArticle(command)
                }

            return runCatching { resolve() }
                .getOrElse { th -> setOf(ExceptionMessage(gson.toAppException(th), command)) }
        }
    }

suspend fun LocalStorage.store(
    article: Article,
): Set<ScreenMessage> = effect {
    insertArticle(article)
    ArticleUpdated(article)
}

suspend fun LocalStorage.remove(
    article: Article,
): Set<ScreenMessage> = effect {
    deleteArticle(article.url)
    ArticleUpdated(article)
}

suspend fun fetch(
    api: NewsApi,
    storage: LocalStorage,
    command: LoadArticlesByQuery,
): Set<ScreenMessage> =
    command.query.effect {

        val (articles, hasMore) = fetch(
            api,
            storage,
            this,
            command.currentSize,
            command.resultsPerPage
        )

        ArticlesLoaded(command.id, articles, hasMore)
    }

suspend fun Application.shareArticle(
    command: DoShareArticle,
): Set<ScreenMessage> = command.sideEffect {
    startActivity(ShareIntent(article))
}

suspend fun fetch(
    api: NewsApi,
    storage: LocalStorage,
    query: Query,
    currentSize: Int,
    resultsPerPage: Int,
): Page =
    when (query.type) {
        Regular -> api.fetchFromEverything(query.input, currentSize, resultsPerPage)
        Favorite -> storage.findAllArticles(query.input)
        Trending -> api.fetchTopHeadlines(query.input, currentSize, resultsPerPage)
    }

fun ShareIntent(
    article: Article,
): Intent =
    Intent().apply {
        action = ACTION_SEND
        putExtra(EXTRA_TEXT, article.url.toExternalForm())
        type = "text/plain"
        putExtra(EXTRA_TITLE, article.title.value)
    }.let { intent ->
        createChooser(intent, null)
            .addFlags(FLAG_ACTIVITY_NEW_TASK)
    }

fun ExceptionMessage(
    th: AppException,
    command: ArticlesCommand,
) = ArticlesOperationException(command.screenId, th)

private val ArticlesCommand.screenId: ScreenId?
    get() = when (this) {
        is LoadArticlesByQuery -> id
        is SaveArticle, is RemoveArticle, is DoShareArticle -> null
    }

suspend fun Gson.toAppException(
    th: Throwable
): AppException =
    th.wrap { raw ->
        when (raw) {
            is IOException -> NetworkException(raw)
            is ClientRequestException -> toAppException(raw)
            else -> null
        }
    } ?: InternalException("An internal exception occurred", th)

private suspend inline fun Throwable.wrap(
    crossinline transform: suspend (Throwable) -> AppException?,
): AppException? =
    if (this is AppException) this
    else transform(this) ?: cause?.let { th -> transform(th) }

private suspend fun Gson.toAppException(
    exception: ClientRequestException,
): AppException =
    NetworkException(
        readErrorMessage(exception) ?: exception.toGenericExceptionDescription(),
        exception
    )

private suspend fun Gson.readErrorMessage(
    exception: ClientRequestException,
) = withContext(Dispatchers.IO) {
    fromJson(exception.response.readText(), JsonObject::class.java)["message"]
        ?.takeUnless { elem -> elem.isJsonNull }
        ?.asString
}

private fun ClientRequestException.toGenericExceptionDescription() =
    "Server returned status code ${response.status.value}"

private fun NetworkException(
    cause: IOException,
) = NetworkException(
    "Network exception occurred, check connectivity",
    cause
)