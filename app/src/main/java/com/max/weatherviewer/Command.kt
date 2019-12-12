package com.max.weatherviewer

import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.home.LoadCriteria
import java.net.URL

sealed class Command

// App wide commands

object CloseApp : Command()

// Feed screen commands

sealed class FeedCommand : Command() {
    abstract val id: ScreenId
}

data class LoadByCriteria(
    override val id: ScreenId,
    val criteria: LoadCriteria
) : FeedCommand()

data class SaveArticle(
    override val id: ScreenId,
    val article: Article
) : FeedCommand()

data class RemoveArticle(
    override val id: ScreenId,
    val url: URL
) : FeedCommand()
