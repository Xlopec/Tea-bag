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

actual interface ArticlesEnv {
    val gson: Gson
    val application: Application
    val storage: LocalStorage
}

fun ArticlesEnv(
    gson: Gson,
    application: Application,
): ArticlesEnv = object : ArticlesEnv {
    override val gson: Gson = gson
    override val application: Application = application
    override val storage: LocalStorage = LocalStorage(application)
}

actual fun <Env> ArticlesResolver(): ArticlesResolver<Env> where Env : ArticlesEnv, Env : NewsApi<Env> =
    ArticlesResolver { command ->
        suspend fun resolve() =
            when (command) {
                is LoadArticlesByQuery -> fetch(command)
                is SaveArticle -> storage.store(command.article)
                is RemoveArticle -> storage.remove(command.article)
                is DoShareArticle -> shareArticle(command)
            }

        runCatching { resolve() }
            .getOrElse { th -> setOf(ExceptionMessage(toAppException(th), command)) }
    }

suspend fun <Env : LocalStorage> Env.store(
    article: Article,
): Set<ScreenMessage> = effect {
    insertArticle(article)
    ArticleUpdated(article)
}

suspend fun <Env : LocalStorage> Env.remove(
    article: Article,
): Set<ScreenMessage> = effect {
    deleteArticle(article.url)
    ArticleUpdated(article)
}

suspend fun <Env> Env.fetch(
    command: LoadArticlesByQuery,
): Set<ScreenMessage> where Env : NewsApi<Env>, Env : ArticlesEnv =
    command.query.effect {

        val (articles, hasMore) = fetch(this, command.currentSize, command.resultsPerPage)

        ArticlesLoaded(command.id, articles, hasMore)
    }

suspend fun <Env : ArticlesEnv> Env.shareArticle(
    command: DoShareArticle,
): Set<ScreenMessage> = command.sideEffect {
    application.startActivity(ShareIntent(article))
}

suspend fun <Env> Env.fetch(
    query: Query,
    currentSize: Int,
    resultsPerPage: Int,
): Page where Env : NewsApi<Env>, Env : ArticlesEnv =
    when (query.type) {
        Regular -> fetchFromEverything(query.input, currentSize, resultsPerPage)
        Favorite -> storage.findAllArticles(query.input)
        Trending -> fetchTopHeadlines(query.input, currentSize, resultsPerPage)
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

suspend fun <Env> Env.toAppException(
    th: Throwable
): AppException where Env : ArticlesEnv =
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

private suspend fun <Env> Env.toAppException(
    exception: ClientRequestException,
): AppException where Env : ArticlesEnv =
    NetworkException(
        gson.readErrorMessage(exception) ?: exception.toGenericExceptionDescription(),
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