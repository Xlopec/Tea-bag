/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
