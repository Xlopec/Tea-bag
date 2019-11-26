package com.max.weatherviewer.home

import com.max.weatherviewer.app.Screen
import com.max.weatherviewer.domain.Article

sealed class Home : Screen()

object Loading : Home()

data class Preview(
    val articles: List<Article>
) : Home()

data class Error(
    val cause: Throwable
) : Home()
