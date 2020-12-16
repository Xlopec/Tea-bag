package com.max.reader.screens.article.list.update

import com.max.reader.app.*
import com.max.reader.domain.Article
import com.max.reader.domain.toggleFavorite
import com.max.reader.screens.article.list.*
import com.max.reader.screens.article.list.QueryType.*
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

// nothing is private in our world
@Suppress("MemberVisibilityCanBePrivate")
object LiveArticlesUpdater : ArticlesUpdater {

    override fun updateArticles(
        message: ArticlesMessage,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command> =
        when {
            // fixme add pagination
            message is ArticlesLoaded -> ArticlesState.preview(state.id, state.query, message.articles).noCommand()
            message is LoadArticles -> ArticlesState.loading(state.id, state.query) command LoadByCriteria(state.id, state.query)
            message is ArticlesOperationException -> ArticlesState.exception(state.id, state.query, message.cause).noCommand()
            message is ToggleArticleIsFavorite && state.isPreview -> toggleFavorite(message.article, state)
            message is ArticleUpdated && state.isPreview -> updateArticle(message.article, state)
            message is ShareArticle -> shareArticle(message.article, state)
            // fixme redesign FeedState
            message is OnQueryUpdated -> updateQuery(message.query, state)
            message is ArticleUpdated -> state.noCommand()// ignore
            else -> error("Can't handle message $message when state is $state")
        }

    fun updateArticle(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command> {

        val updated = when (state.query.type) {
            Regular, Trending -> state.updateArticle(article)
            Favorite -> if (article.isFavorite) state.prependArticle(article) else state.removeArticle(article)
        }

        return updated.noCommand()
    }

    fun toggleFavorite(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command> {

        val toggled = article.toggleFavorite()

        return state.updateArticle(toggled) command toggled.storeCommand()
    }

    fun shareArticle(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, DoShareArticle> = state command DoShareArticle(article)

    fun updateQuery(
        input: String,
        state: ArticlesState
    ): UpdateWith<ArticlesState, ArticlesCommand> = state.updateQuery(input).noCommand()

    fun Article.storeCommand() = if (isFavorite) SaveArticle(this) else RemoveArticle(this)

    // fixme inline
    fun ArticlesState.updateQuery(
        input: String
    ) = copy(query = query.copy(input = input))

}