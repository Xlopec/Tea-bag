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

package com.oliynick.max.reader.app.feature.article.details

import android.app.Application
import android.content.Intent
import android.net.Uri
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.ScreenMessage
import com.oliynick.max.reader.app.command.ArticleDetailsCommand
import com.oliynick.max.reader.app.command.DoOpenArticle
import com.oliynick.max.tea.core.component.sideEffect

fun ArticleDetailsResolver(
    application: Application
): ArticleDetailsResolver =
    object : ArticleDetailsResolver {

        override suspend fun resolve(
            command: ArticleDetailsCommand
        ): Set<Message> =
            when (command) {
                is DoOpenArticle -> openArticle(command)
            }

        suspend fun openArticle(
            command: DoOpenArticle
        ): Set<ScreenMessage> = command.sideEffect {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url.toString()))
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

            if (intent.resolveActivity(application.packageManager) != null) {
                application.startActivity(intent)
            }
        }

    }
