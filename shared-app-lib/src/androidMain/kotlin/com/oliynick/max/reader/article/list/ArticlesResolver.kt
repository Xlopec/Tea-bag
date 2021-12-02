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
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.app.command.ArticlesCommand
import com.oliynick.max.reader.app.command.DoShareArticle
import com.oliynick.max.reader.app.command.LoadArticlesByQuery
import com.oliynick.max.reader.app.command.RemoveArticle
import com.oliynick.max.reader.app.command.SaveArticle
import com.oliynick.max.reader.app.message.Message
import com.oliynick.max.reader.app.message.ScreenMessage
import com.oliynick.max.reader.app.storage.LocalStorage
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> ArticlesResolver(
    application: Application,
): ArticlesResolver<Env> where Env : NewsApi, Env : LocalStorage =
    ArticlesResolverImpl(application)
/*object : ArticlesResolver<Env> {
    override suspend fun Env.resolve(
        command: ArticlesCommand
    ): Set<Message> =
        when (command) {
            is LoadArticlesByQuery -> loadArticles(command)
            is SaveArticle -> storeArticle(command.article)
            is RemoveArticle -> removeArticle(command.article)
            is DoShareArticle -> application.shareArticle(command)
        }
}*/

private class ArticlesResolverImpl<Env>(
    private val application: Application
) : ArticlesResolver<Env> where Env : NewsApi, Env : LocalStorage {
    override suspend fun Env.resolve(
        command: ArticlesCommand
    ): Set<Message> =
        when (command) {
            is LoadArticlesByQuery -> loadArticles(command)
            is SaveArticle -> storeArticle(command.article)
            is RemoveArticle -> removeArticle(command.article)
            is DoShareArticle -> application.shareArticle(command)
        }
}

private suspend fun Application.shareArticle(
    command: DoShareArticle,
): Set<ScreenMessage> = command.sideEffect {
    startActivity(ShareIntent(article))
}

private fun ShareIntent(
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