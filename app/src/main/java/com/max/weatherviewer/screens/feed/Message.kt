package com.max.weatherviewer.screens.feed

import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.app.exception.AppException
import com.max.weatherviewer.domain.Article

sealed class ScreenMessage

sealed class FeedMessage : ScreenMessage()

data class LoadArticles(
    val id: ScreenId
) : FeedMessage()

data class ToggleArticleIsFavorite(
    val id: ScreenId,
    val article: Article
) : FeedMessage()

data class ArticlesLoaded(
    val id: ScreenId,
    val articles: List<Article>
) : FeedMessage()

data class FeedOperationException(
    val id: ScreenId?,
    val cause: AppException
) : FeedMessage()

data class ArticleUpdated(
    val article: Article
) : FeedMessage()

data class OpenArticle(
    val article: Article
) : FeedMessage()

data class ShareArticle(
    val article: Article
) : FeedMessage()

data class OnQueryUpdated(
    val id: ScreenId,
    val query: String
) : FeedMessage()
