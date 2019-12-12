package com.max.weatherviewer

import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.home.LoadCriteria

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
