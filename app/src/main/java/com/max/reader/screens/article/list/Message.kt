package com.max.reader.screens.article.list

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenMessage
import com.max.reader.app.exception.AppException
import com.max.reader.domain.Article

sealed class ArticlesMessage : ScreenMessage()

data class LoadArticles(
    val id: ScreenId
) : ArticlesMessage()

data class ToggleArticleIsFavorite(
    val id: ScreenId,
    val article: Article
) : ArticlesMessage()

data class ArticlesLoaded(
    val id: ScreenId,
    val articles: List<Article>
) : ArticlesMessage()

data class ArticlesOperationException(
    val id: ScreenId?,
    val cause: AppException
) : ArticlesMessage()

data class ArticleUpdated(
    val article: Article
) : ArticlesMessage()

data class ShareArticle(
    val article: Article
) : ArticlesMessage()

data class OnQueryUpdated(
    val id: ScreenId,
    val query: String
) : ArticlesMessage()
