package com.max.weatherviewer.home

import com.max.weatherviewer.app.Screen
import com.max.weatherviewer.domain.Article
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

sealed class Feed : Screen() {
    abstract val criteria: LoadCriteria
}

sealed class LoadCriteria {

    data class Query(
        val query: String
    ) : LoadCriteria()

    object Favorite : LoadCriteria()

    object Trending : LoadCriteria()
}

data class FeedLoading(
    override val criteria: LoadCriteria
) : Feed()

data class Preview(
    override val criteria: LoadCriteria,
    val articles: List<Article>,
    val screens: ImmutableList<Screen> = immutableListOf()
) : Feed()

data class Error(
    override val criteria: LoadCriteria,
    val cause: Throwable
) : Feed()
