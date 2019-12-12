package com.max.weatherviewer.home

import com.max.weatherviewer.app.ScreenId
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

data class ArticlesLoadException(
    val id: ScreenId,
    val cause: Throwable
) : FeedMessage()

data class ArticleUpdated(
    val article: Article
) : FeedMessage()