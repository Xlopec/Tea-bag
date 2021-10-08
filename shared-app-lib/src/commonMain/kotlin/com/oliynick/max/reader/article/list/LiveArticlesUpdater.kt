/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.domain.toggleFavorite
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
