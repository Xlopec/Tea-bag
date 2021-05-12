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

package com.max.reader.screens.article.details.resolve

import android.content.Intent
import android.net.Uri
import com.max.reader.app.ArticleDetailsCommand
import com.max.reader.app.DoOpenArticle
import com.max.reader.app.Message
import com.max.reader.app.ScreenMessage
import com.max.reader.app.env.HasAppContext
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> LiveArticleDetailsResolver(): ArticleDetailsResolver<Env> where Env : HasAppContext =
    object : ArticleDetailsResolver<Env> {

        override suspend fun Env.resolve(command: ArticleDetailsCommand): Set<Message> =
            when(command) {
                is DoOpenArticle -> openArticle(command)
            }

        suspend fun Env.openArticle(
            command: DoOpenArticle
        ): Set<ScreenMessage> = command.sideEffect {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url.toString()))
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

            if (intent.resolveActivity(application.packageManager) != null) {
                application.startActivity(intent)
            }
        }

    }
