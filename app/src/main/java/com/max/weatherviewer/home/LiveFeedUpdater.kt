package com.max.weatherviewer.home

import com.max.weatherviewer.Command
import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.RemoveArticle
import com.max.weatherviewer.SaveArticle
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.domain.toggleFavorite
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

    override fun update(
        message: FeedMessage,
        feed: Feed
    ): UpdateWith<Feed, Command> =
        when {
            message is ArticlesLoaded -> Preview(feed.id, feed.criteria, message.articles).noCommand()
            message is LoadArticles -> FeedLoading(feed.id, feed.criteria) command LoadByCriteria(feed.id, feed.criteria)
            message is ArticlesLoadException -> Error(feed.id, feed.criteria, message.cause).noCommand()
            message is ToggleArticleIsFavorite && feed is Preview -> markArticleAsFavorite(message.article, feed)
            else -> error("Can't handle message $message when state is $feed")
        }

    fun markArticleAsFavorite(
        article: Article,
        state: Preview
    ): UpdateWith<Feed, Command> {

        val toggled = article.toggleFavorite()

        return state.updateArticle(toggled) command toggled.storeCommand(state.id)
    }

    fun Article.storeCommand(id: ScreenId) =
        if (isFavorite) SaveArticle(id, this) else RemoveArticle(id, url)

}