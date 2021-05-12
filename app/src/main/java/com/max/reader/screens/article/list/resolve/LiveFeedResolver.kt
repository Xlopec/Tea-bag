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
import com.max.reader.app.*
import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.Storage
import com.max.reader.app.exception.AppException
import com.max.reader.app.exception.toAppException
import com.max.reader.domain.Article
import com.max.reader.screens.article.list.ArticleUpdated
import com.max.reader.screens.article.list.ArticlesLoaded
import com.max.reader.screens.article.list.ArticlesOperationException
import com.max.reader.screens.article.list.Query
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect

@Deprecated("wait until it'll be fixed")
fun <Env> LiveArticlesResolver(): ArticlesResolver<Env> where Env : HasAppContext,
                                                              Env : HasGson,
                                                              Env : Storage<Env> =
    object : ArticlesResolver<Env> {
        override suspend fun Env.resolve(
            command: ArticlesCommand,
        ): Set<ScreenMessage> {

            suspend fun resolve() =
                when (command) {
                    is LoadArticlesByQuery -> fetch(command.id, command.query, command.currentSize, command.resultsPerPage)
                    is SaveArticle -> store(command.article)
                    is RemoveArticle -> remove(command.article)
                    is DoShareArticle -> shareArticle(command)
                }

            return runCatching { resolve() }
                .getOrElse { th -> setOf(toErrorMessage(toAppException(th), command)) }
        }

        suspend fun Env.store(
            article: Article,
        ): Set<ScreenMessage> = effect {
            addToFavorite(article)
            ArticleUpdated(
                article
            )
        }

        suspend fun Env.remove(
            article: Article,
        ): Set<ScreenMessage> = effect {
            removeFromFavorite(article.url)
            ArticleUpdated(
                article
            )
        }

        suspend fun Env.fetch(
            id: ScreenId,
            query: Query,
            currentSize: Int,
            resultsPerPage: Int
        ): Set<ScreenMessage> = query.effect {

            val (articles, hasMore) = fetch(this, currentSize, resultsPerPage)

            ArticlesLoaded(id, articles, hasMore)
        }

        suspend fun Env.shareArticle(
            command: DoShareArticle,
        ): Set<ScreenMessage> = command.sideEffect {
            application.startActivity(createShareIntent(article))
        }

        fun createShareIntent(
            article: Article,
        ): Intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, article.url.toExternalForm())
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, article.title.value)
            }.let { intent ->
                Intent.createChooser(intent, null)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        fun toErrorMessage(
            th: AppException,
            command: ArticlesCommand,
        ) = ArticlesOperationException(
            command.screenId(),
            th
        )

    }

private fun ArticlesCommand.screenId(): ScreenId? =
    when (this) {
        is LoadArticlesByQuery -> id
        is SaveArticle, is RemoveArticle, is DoShareArticle -> null
    }
