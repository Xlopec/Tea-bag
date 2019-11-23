package com.max.weatherviewer.home

import com.max.weatherviewer.domain.Article

sealed class HomeState

object Loading : HomeState()

data class Preview(
    val articles: List<Article>
) : HomeState()

data class Error(
    val cause: Throwable
) : HomeState()
