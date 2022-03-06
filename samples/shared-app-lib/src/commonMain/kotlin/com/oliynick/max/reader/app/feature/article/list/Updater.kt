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
import com.oliynick.max.reader.app.feature.article.list.FilterType.*
import com.oliynick.max.reader.app.feature.article.list.Paging.Companion.FirstPage
import com.oliynick.max.reader.app.misc.isPreview
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

fun updateArticles(
    message: ArticlesMessage,
    state: ArticlesState
): UpdateWith<ArticlesState, Command> =
    when (message) {
        is ArticlesLoaded -> state.toPreviewUpdate(message.page)
        is LoadNextArticles -> state.toLoadNextUpdate()
        is LoadArticles -> state.toLoadUpdate()
        is RefreshArticles -> state.toRefreshUpdate()
        is ArticlesOperationException -> state.toOperationExceptionUpdate(message)
        is ToggleArticleIsFavorite -> state.toToggleFavoriteUpdate(message.article)
        is OnArticleUpdated -> state.toUpdateAllArticlesUpdate(message.article)
        is OnShareArticle -> state.toShareArticleUpdate(message.article)
        is OnQueryUpdated -> state.toQueryUpdate(message.query)
        is SyncScrollPosition -> state.toSyncScrollStateUpdate(message)
    }

private fun ArticlesState.toOperationExceptionUpdate(
    message: ArticlesOperationException
) = toException(message.cause).noCommand()

private fun ArticlesState.toPreviewUpdate(
    page: Page<Article>
) = toPreview(page).noCommand()

private fun ArticlesState.toRefreshUpdate() = toRefreshing() command toLoadArticlesQuery(FirstPage)

private fun ArticlesState.toLoadArticlesQuery(
    paging: Paging
) = LoadArticlesByQuery(id, filter, paging)

private fun ArticlesState.toLoadUpdate() = run {
    toLoading() command setOfNotNull(
        toLoadArticlesQuery(FirstPage), filter.toSanitized()?.let(::StoreSearchQuery)
    )
}

private fun Filter.toSanitized() =
    input.takeUnless(CharSequence::isEmpty)?.trim()?.let { Filter(it, type) }

private fun ArticlesState.toLoadNextUpdate() =
    if (loadable.isPreview && loadable.hasMore && loadable.data.isNotEmpty() /*todo should we throw an error in this case?*/) {
        toLoadingNext() command toLoadArticlesQuery(nextPage())
    } else {
        // just ignore the command
        noCommand()
    }

private fun ArticlesState.toUpdateAllArticlesUpdate(
    article: Article
): UpdateWith<ArticlesState, Command> {

    val updated = when (filter.type) {
        Regular, Trending -> updateArticle(article)
        Favorite -> if (article.isFavorite) prependArticle(article) else removeArticle(
            article
        )
    }

    return updated.noCommand()
}

private fun ArticlesState.toToggleFavoriteUpdate(
    article: Article
): UpdateWith<ArticlesState, Command> {

    val toggled = article.toggleFavorite()

    return updateArticle(toggled) command toggled.storeCommand()
}

private fun ArticlesState.toShareArticleUpdate(
    article: Article
): UpdateWith<ArticlesState, DoShareArticle> = this command DoShareArticle(article)

private fun ArticlesState.toQueryUpdate(
    input: String
): UpdateWith<ArticlesState, ArticlesCommand> =
    copy(filter = filter.update(input)).noCommand()

private fun Article.storeCommand() = if (isFavorite) SaveArticle(this) else RemoveArticle(this)

private fun ArticlesState.toSyncScrollStateUpdate(
    message: SyncScrollPosition
) = copy(scrollState = message.scrollState).noCommand()
