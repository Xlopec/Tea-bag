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
import com.max.reader.app.ScreenMessage
import com.max.reader.app.exception.AppException
import com.max.reader.domain.Article

sealed class ArticlesMessage : ScreenMessage()

data class LoadNextArticles(
    val id: ScreenId,
) : ArticlesMessage()

data class LoadArticlesFromScratch(
    val id: ScreenId,
) : ArticlesMessage()

data class RefreshArticles(
    val id: ScreenId,
) : ArticlesMessage()

data class ToggleArticleIsFavorite(
    val id: ScreenId,
    val article: Article,
) : ArticlesMessage()

data class ArticlesLoaded(
    val id: ScreenId,
    val articles: List<Article>,
    val hasMore: Boolean,
) : ArticlesMessage()

data class ArticlesOperationException(
    val id: ScreenId?,
    val cause: AppException,
) : ArticlesMessage()

data class ArticleUpdated(
    val article: Article,
) : ArticlesMessage()

data class ShareArticle(
    val article: Article,
) : ArticlesMessage()

data class OnQueryUpdated(
    val id: ScreenId,
    val query: String,
) : ArticlesMessage()
