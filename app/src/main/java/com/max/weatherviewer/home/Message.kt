package com.max.weatherviewer.home

import com.max.weatherviewer.app.ScreenMessage
import com.max.weatherviewer.domain.Article

sealed class HomeMessage : ScreenMessage()

data class LoadArticles(
    val query: String
) : HomeMessage()

data class ArticlesLoaded(
    val articles: List<Article>
) : HomeMessage()

data class ArticlesLoadException(
    val cause: Throwable
) : HomeMessage()
