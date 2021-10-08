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

package com.max.reader.screens.article.list.resolve

import android.content.Intent
import android.content.Intent.*
import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.network.NewsApi
import com.max.reader.app.exception.toAppException
import com.oliynick.max.reader.app.ScreenMessage
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.article.list.*
import com.oliynick.max.reader.article.list.ArticlesResolver
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.network.Page
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> LiveArticlesResolver(): ArticlesResolver<Env> where Env : HasAppContext,
                                                              Env : HasGson,
                                                              Env : NewsApi<Env>,
                                                              Env : LocalStorage =
    ArticlesResolver { command ->
        suspend fun resolve() =
            when (command) {
                is LoadArticlesByQuery -> fetch(command)
                is SaveArticle -> store(command.article)
                is RemoveArticle -> remove(command.article)
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
): Set<ScreenMessage> where Env : NewsApi<Env>, Env : LocalStorage =
    command.query.effect {

        val (articles, hasMore) = fetch(this, command.currentSize, command.resultsPerPage)

        ArticlesLoaded(command.id, articles, hasMore)
    }

suspend fun <Env : HasAppContext> Env.shareArticle(
    command: DoShareArticle,
): Set<ScreenMessage> = command.sideEffect {
    application.startActivity(ShareIntent(article))
}

suspend fun <Env> Env.fetch(
    query: Query,
    currentSize: Int,
    resultsPerPage: Int,
): Page where Env : NewsApi<Env>, Env : LocalStorage =
    when (query.type) {
        Regular -> fetchFromEverything(query.input, currentSize, resultsPerPage)
        Favorite -> findAllArticles(query.input)
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
