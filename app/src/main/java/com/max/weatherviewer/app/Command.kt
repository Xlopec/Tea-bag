package com.max.weatherviewer.app

import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.screens.feed.LoadCriteria

sealed class Command

// App wide commands

object CloseApp : Command()

// Feed screen commands

sealed class FeedCommand : Command()

data class LoadByCriteria(
    val id: ScreenId,
    val criteria: LoadCriteria
) : FeedCommand()

data class SaveArticle(
    val article: Article
) : FeedCommand()

data class RemoveArticle(
    val article: Article
) : FeedCommand()

data class DoOpenArticle(
    val article: Article
) : FeedCommand()

data class DoShareArticle(
    val article: Article
) : FeedCommand()
