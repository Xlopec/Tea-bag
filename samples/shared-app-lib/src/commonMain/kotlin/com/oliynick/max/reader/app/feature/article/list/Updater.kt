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

package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.domain.toggleFavorite
import com.oliynick.max.reader.app.feature.article.list.Paging.Companion.FirstPage
import com.oliynick.max.reader.app.feature.article.list.QueryType.*
import com.oliynick.max.reader.app.feature.isPreview
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

fun updateArticles(
    message: ArticlesMessage,
    state: ArticlesState
): UpdateWith<ArticlesState, Command> =
    // fixme refactor this bit
    when (message) {
        is ArticlesLoaded -> state.toPreview(message.page).noCommand()
        is LoadNextArticles -> loadNextArticle(state)
        is LoadArticles -> loadArticles(state)
        is RefreshArticles -> refreshArticles(state)
        is ArticlesOperationException -> state.toException(message.cause).noCommand()
        is ToggleArticleIsFavorite -> toggleFavorite(message.article, state)
        is OnArticleUpdated -> updateArticle(message.article, state)
        is OnShareArticle -> shareArticle(message.article, state)
        // fixme redesign FeedState
        is OnQueryUpdated -> updateQuery(message.query, state)
        is SyncScrollPosition -> updateScrollState(state, message)
    }

private fun refreshArticles(
    articlesState: ArticlesState
) = articlesState.toRefreshing() command articlesState.toLoadArticlesQuery(FirstPage)

private fun ArticlesState.toLoadArticlesQuery(
    paging: Paging
) = LoadArticlesByQuery(id, query, paging)

private fun loadArticles(
    articlesState: ArticlesState
) = articlesState.run {
    toLoading() command setOfNotNull(
        toLoadArticlesQuery(FirstPage), query.toSanitized()?.let(::StoreSearchQuery)
    )
}

private fun Query.toSanitized() =
    input.takeUnless(CharSequence::isEmpty)?.trim()?.let { Query(it, type) }

private fun loadNextArticle(
    articlesState: ArticlesState
) =
    if (articlesState.loadable.isPreview && articlesState.loadable.hasMore && articlesState.loadable.data.isNotEmpty() /*todo should we throw an error in this case?*/) {
        articlesState.toLoadingNext() command articlesState.toLoadArticlesQuery(articlesState.nextPage())
    } else {
        // just ignore the command
        articlesState.noCommand()
    }

private fun updateArticle(
    article: Article,
    state: ArticlesState
): UpdateWith<ArticlesState, Command> {

    val updated = when (state.query.type) {
        Regular, Trending -> state.updateArticle(article)
        Favorite -> if (article.isFavorite) state.prependArticle(article) else state.removeArticle(
            article
        )
    }

    return updated.noCommand()
}

private fun toggleFavorite(
    article: Article,
    state: ArticlesState
): UpdateWith<ArticlesState, Command> {

    val toggled = article.toggleFavorite()

    return state.updateArticle(toggled) command toggled.storeCommand()
}

private fun shareArticle(
    article: Article,
    state: ArticlesState
): UpdateWith<ArticlesState, DoShareArticle> = state command DoShareArticle(article)

private fun updateQuery(
    input: String,
    state: ArticlesState
): UpdateWith<ArticlesState, ArticlesCommand> =
    state.copy(query = state.query.update(input)).noCommand()

private fun Article.storeCommand() = if (isFavorite) SaveArticle(this) else RemoveArticle(this)

private fun updateScrollState(
    state: ArticlesState,
    message: SyncScrollPosition
) = state.copy(scrollState = message.scrollState).noCommand()
