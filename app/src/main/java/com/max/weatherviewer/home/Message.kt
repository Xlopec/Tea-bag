package com.max.weatherviewer.home

import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article

sealed class ScreenMessage {
    abstract val id: ScreenId
}

sealed class FeedMessage : ScreenMessage()

data class LoadArticles(
    override val id: ScreenId,
    val query: String
) : FeedMessage()

data class ArticlesLoaded(
    override val id: ScreenId,
    val articles: List<Article>
) : FeedMessage()

data class ArticlesLoadException(
    override val id: ScreenId,
    val cause: Throwable
) : FeedMessage()
