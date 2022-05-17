/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.reader.app.feature.article.list

import io.github.xlopec.reader.app.FilterUpdated
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.feature.article.list.Paging.Companion.FirstPage
import io.github.xlopec.reader.app.misc.isIdle
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType.Favorite
import io.github.xlopec.reader.app.model.FilterType.Regular
import io.github.xlopec.reader.app.model.FilterType.Trending
import io.github.xlopec.reader.app.model.toggleFavorite
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand

fun ArticlesState.toArticlesUpdate(
    message: ArticlesMessage,
): Update<ArticlesState, Command> =
    when (message) {
        is ArticlesLoaded -> toIdleUpdate(message.page)
        is LoadNextArticles -> toLoadNextUpdate()
        is LoadArticles -> toLoadUpdate()
        is RefreshArticles -> toRefreshUpdate()
        is ArticlesOperationException -> toOperationExceptionUpdate(message)
        is ToggleArticleIsFavorite -> toToggleFavoriteUpdate(message.article)
        is ArticleUpdated -> toUpdateAllArticlesUpdate(message.article)
        is OnShareArticle -> toShareArticleUpdate(message.article)
        is SyncScrollPosition -> toSyncScrollStateUpdate(message)
        is FilterLoaded -> toLoadUpdate(message.filter)
    }

fun updateArticles(
    message: FilterUpdated,
    state: ArticlesState,
) = if (message.filter.type == state.filter.type) state.toFilterUpdate(message.filter) else state.noCommand()

private fun ArticlesState.toOperationExceptionUpdate(
    message: ArticlesOperationException,
) = toException(message.cause).noCommand()

private fun ArticlesState.toIdleUpdate(
    page: Page<Article>,
) = toIdle(page).noCommand()

private fun ArticlesState.toRefreshUpdate() = toRefreshing() command toLoadCommand(FirstPage)

private fun ArticlesState.toLoadCommand(
    paging: Paging,
    filter: Filter = this.filter,
) = DoLoadArticles(id, filter, paging)

private fun ArticlesState.toLoadUpdate(
    newFilter: Filter = filter,
) = toLoading(filter = newFilter).command(
    toLoadCommand(FirstPage, newFilter),
    DoStoreFilter(newFilter)
)

private fun ArticlesState.toLoadNextUpdate() =
    if (loadable.isIdle && loadable.hasMore && loadable.data.isNotEmpty() /*todo should we throw an error in this case?*/) {
        toLoadingNext() command toLoadCommand(nextPage())
    } else {
        // just ignore the command
        noCommand()
    }

private fun ArticlesState.toUpdateAllArticlesUpdate(
    article: Article,
) = when (filter.type) {
    Regular, Trending -> updateArticle(article).noCommand()
    Favorite -> toFavoriteArticleUpdate(article)
}

private fun ArticlesState.toFavoriteArticleUpdate(
    article: Article,
): Update<ArticlesState, ArticlesCommand> {
    require(filter.type == Favorite)
    // if article was marked as favorite, then we should perform full page reload from DB
    return if (article.isFavorite) toLoadUpdate() else removeArticle(article).noCommand()
}

private fun ArticlesState.toToggleFavoriteUpdate(
    article: Article,
): Update<ArticlesState, Command> {

    val toggled = article.toggleFavorite()

    return updateArticle(toggled) command toggled.storeCommand()
}

private fun ArticlesState.toShareArticleUpdate(
    article: Article,
): Update<ArticlesState, DoShareArticle> = this command DoShareArticle(article)

private fun ArticlesState.toFilterUpdate(
    filter: Filter,
): Update<ArticlesState, ArticlesCommand> =
    copy(filter = filter).noCommand()

private fun Article.storeCommand() = if (isFavorite) DoSaveArticle(this) else DoRemoveArticle(this)

private fun ArticlesState.toSyncScrollStateUpdate(
    message: SyncScrollPosition,
) = copy(scrollState = message.scrollState).noCommand()
