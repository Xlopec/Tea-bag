package com.max.weatherviewer.home

import com.max.weatherviewer.Command
import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.RemoveArticle
import com.max.weatherviewer.SaveArticle
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

// nothing is private in our world
@Suppress("MemberVisibilityCanBePrivate")
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
            message is ArticleUpdated && feed is Preview -> updateArticle(message.article, feed)
            message is ArticleUpdated -> feed.noCommand()// ignore
            else -> error("Can't handle message $message when state is $feed")
        }

    fun updateArticle(
        article: Article,
        state: Preview
    ): UpdateWith<Feed, Command> {

        val updated = when(state.criteria) {
            is LoadCriteria.Query, LoadCriteria.Trending -> state.updateArticle(article)
            LoadCriteria.Favorite -> if (article.isFavorite) state.prependArticle(article) else state.removeArticle(article)
        }

        return updated.noCommand()
    }

    fun markArticleAsFavorite(
        article: Article,
        state: Preview
    ): UpdateWith<Feed, Command> {

        val toggled = article.toggleFavorite()

        return state.updateArticle(toggled) command toggled.storeCommand()
    }

    fun Article.storeCommand() = if (isFavorite) SaveArticle(this) else RemoveArticle(this)

}