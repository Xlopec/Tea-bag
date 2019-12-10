package com.max.weatherviewer.home

import com.max.weatherviewer.Command
import com.max.weatherviewer.LoadByCriteria
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

interface FeedUpdater {
    fun update(
        message: FeedMessage,
        feed: Feed
    ): UpdateWith<Feed, Command>
}

object LiveFeedUpdater : FeedUpdater {

    override fun update(message: FeedMessage, feed: Feed): UpdateWith<Feed, Command> =
        when (message) {
            is ArticlesLoaded -> Preview(feed.id, feed.criteria, message.articles).noCommand()
            is LoadArticles -> FeedLoading(feed.id, feed.criteria) command LoadByCriteria(feed.id, feed.criteria)
            is ArticlesLoadException -> Error(feed.id, feed.criteria, message.cause).noCommand()
        }

}