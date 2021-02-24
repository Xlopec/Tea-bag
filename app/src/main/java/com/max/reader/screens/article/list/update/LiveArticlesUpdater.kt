/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        when (message) {
            is ArticlesLoaded -> state.toPreview(message.articles, message.hasMore).noCommand()
            is LoadNextArticles -> loadNextArticles(state)
            is LoadArticlesFromScratch -> state.toLoading() command state.toLoadArticlesQuery()
            is RefreshArticles -> state.toRefreshing() command state.toLoadArticlesQuery()
            is ArticlesOperationException -> state.toException(message.cause).noCommand()
            is ToggleArticleIsFavorite -> toggleFavorite(message.article, state)
            is ArticleUpdated -> updateArticle(message.article, state)
            is ShareArticle -> shareArticle(message.article, state)
            // fixme redesign FeedState
            is OnQueryUpdated -> updateQuery(message.query, state)
        }

    fun ArticlesState.toLoadArticlesQuery() =
        LoadArticlesByQuery(id, query, articles.size, ArticlesState.ArticlesPerPage)

    fun loadNextArticles(
        state: ArticlesState
    ) =
        if (state.isPreview && state.hasMoreArticles) {
            state.toLoadingNext() command LoadArticlesByQuery(
                state.id,
                state.query,
                state.articles.size,
                ArticlesState.ArticlesPerPage
            )
        } else {
            state.noCommand()
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
