package com.max.weatherviewer.home

import com.max.weatherviewer.Command
import com.max.weatherviewer.LoadByCriteria
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

interface HomeUpdater {
    fun update(
        message: HomeMessage,
        feed: Feed
    ): UpdateWith<Feed, Command>
}

object LiveHomeUpdater : HomeUpdater {

    override fun update(message: HomeMessage, feed: Feed): UpdateWith<Feed, Command> =
        when (message) {
            is ArticlesLoaded -> Preview(feed.criteria, message.articles).noCommand()
            is LoadArticles -> FeedLoading(feed.criteria) command LoadByCriteria(feed.criteria)
            is ArticlesLoadException -> Error(feed.criteria, message.cause).noCommand()
        }

}