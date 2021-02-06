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

package com.max.reader.screens.article.details.update

import com.max.reader.app.ArticleDetailsCommand
import com.max.reader.app.DoOpenArticle
import com.max.reader.screens.article.details.ArticleDetailsMessage
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.OpenInBrowser
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command

object LiveArticleDetailsUpdater : ArticleDetailsUpdater {

    override fun updateArticleDetails(
        message: ArticleDetailsMessage,
        screen: ArticleDetailsState,
    ): UpdateWith<ArticleDetailsState, ArticleDetailsCommand> =
        when(message) {
            is OpenInBrowser -> screen command DoOpenArticle(screen.article)
        }
}
