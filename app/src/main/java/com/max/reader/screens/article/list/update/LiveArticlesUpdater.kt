package com.max.reader.screens.article.list.update

import com.max.reader.app.*
import com.max.reader.domain.Article
import com.max.reader.domain.toggleFavorite
import com.max.reader.screens.article.list.*
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

// nothing is private in our world
@Suppress("MemberVisibilityCanBePrivate")
object LiveArticlesUpdater : ArticlesUpdater {

    override fun update(
        message: ArticlesMessage,
        articles: ArticlesState
    ): UpdateWith<ArticlesState, Command> =
        when {
            message is ArticlesLoaded -> ArticlesPreviewState(articles.id, articles.criteria, message.articles).noCommand()
            message is LoadArticles -> ArticlesLoadingState(articles.id, articles.criteria) command LoadByCriteria(articles.id, articles.criteria)
            message is ArticlesOperationException -> ArticlesErrorState(articles.id, articles.criteria, message.cause).noCommand()
            message is ToggleArticleIsFavorite && articles is ArticlesPreviewState -> toggleFavorite(message.article, articles)
            message is ArticleUpdated && articles is ArticlesPreviewState -> updateArticle(message.article, articles)
            message is ShareArticle -> shareArticle(message.article, articles)
            // fixme redesign FeedState
            message is OnQueryUpdated && articles.criteria is LoadCriteria.Query -> updateQuery(message.query, articles.criteria as LoadCriteria.Query, articles)
            message is ArticleUpdated -> articles.noCommand()// ignore
            else -> error("Can't handle message $message when state is $articles")
        }

    fun updateArticle(
        article: Article,
        state: ArticlesPreviewState
    ): UpdateWith<ArticlesState, Command> {

        val updated = when (state.criteria) {
            is LoadCriteria.Query, LoadCriteria.Trending -> state.updateArticle(article)
            LoadCriteria.Favorite -> if (article.isFavorite) state.prependArticle(article) else state.removeArticle(article)
        }

        return updated.noCommand()
    }

    fun toggleFavorite(
        article: Article,
        state: ArticlesPreviewState
    ): UpdateWith<ArticlesState, Command> {

        val toggled = article.toggleFavorite()

        return state.updateArticle(toggled) command toggled.storeCommand()
    }

    fun shareArticle(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, DoShareArticle> = state command DoShareArticle(article)

    fun updateQuery(
        query: String,
        criteria: LoadCriteria.Query,
        state: ArticlesState
    ): UpdateWith<ArticlesState, ArticlesCommand> = when (state) {
        is ArticlesLoadingState -> state.copy(criteria = criteria.copy(query = query))
        is ArticlesPreviewState -> state.copy(criteria = criteria.copy(query = query))
        is ArticlesErrorState -> state.copy(criteria = criteria.copy(query = query))
    }.noCommand()

    fun Article.storeCommand() = if (isFavorite) SaveArticle(this) else RemoveArticle(this)

}