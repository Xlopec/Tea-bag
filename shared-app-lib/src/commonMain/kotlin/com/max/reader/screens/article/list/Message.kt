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

package com.max.reader.screens.article.list

import com.max.reader.app.ScreenId
import com.max.reader.app.AppException
import com.max.reader.app.message.ScreenMessage
import com.oliynick.max.reader.domain.Article

sealed interface ArticlesMessage : ScreenMessage {
    val id: ScreenId?
}

data class LoadNextArticles(
    override val id: ScreenId,
) : ArticlesMessage

data class LoadArticlesFromScratch(
    override val id: ScreenId,
) : ArticlesMessage

data class RefreshArticles(
    override val id: ScreenId,
) : ArticlesMessage

data class ToggleArticleIsFavorite(
    override val id: ScreenId,
    val article: Article,
) : ArticlesMessage

data class ArticlesLoaded(
    override val id: ScreenId,
    val articles: List<Article>,
    val hasMore: Boolean,
) : ArticlesMessage

data class ArticlesOperationException(
    override val id: ScreenId?,
    val cause: AppException,
) : ArticlesMessage

data class ArticleUpdated(
    val article: Article,
) : ArticlesMessage {
    override val id: Nothing? = null
}

data class ShareArticle(
    val article: Article,
) : ArticlesMessage {
    override val id: Nothing? = null
}

data class OnQueryUpdated(
    override val id: ScreenId,
    val query: String,
) : ArticlesMessage
