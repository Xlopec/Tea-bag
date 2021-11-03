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

@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.settings.SettingsState
import com.oliynick.max.reader.article.list.ArticlesState
import com.oliynick.max.reader.article.list.Query
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.article.details.ArticleDetailsState
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

fun interface AppNavigation {

    fun navigate(
        nav: Navigation,
        state: AppState,
    ): UpdateWith<AppState, Command>
}

fun AppNavigation(
    debug: Boolean = true
) = AppNavigation { nav, state ->
    when (nav) {
        is TabNavigation -> state.pushTabIfNotExists(nav, TabNavigation::toTabScreen)
        // gson serializer breaks singletons identity, thus we should rely on `is` check rather
        // then referential equality
        is NavigateToArticleDetails -> state.pushArticleDetailsScreen(nav)
        is Pop -> state.pop()
    }
}

// todo refactor later
fun TabNavigation.toTabScreen(): UpdateWith<TabScreen, Command> =
    when (this) {
        NavigateToSettings -> SettingsState.noCommand()
        NavigateToFeed -> Query("android", Regular).let { ArticlesState.newLoading(id, it) command LoadArticlesByQuery(id, it) }
        NavigateToFavorite -> Query("", Favorite).let { ArticlesState.newLoading(id, it) command LoadArticlesByQuery(id, it) }
        NavigateToTrending -> Query("", Trending).let { ArticlesState.newLoading(id, it) command LoadArticlesByQuery(id, it) }
    }

inline fun AppState.pushTabIfNotExists(
    nav: TabNavigation,
    crossinline screenWithCommand: (TabNavigation) -> UpdateWith<TabScreen, Command>,
): UpdateWith<AppState, Command> {
    val i = findTabScreenIndex(nav)
    val screenToCommand by lazy { screenWithCommand(nav) }
    val swap = i >= 0
    val newState = if (swap) {
        // move current screen to the start of screens stack,
        // so that it'll be popped out first
        swapScreens(i, 0)
    } else {
        pushScreen(screenToCommand.first)
    }

    return (if (swap) newState.noCommand() else newState command screenToCommand.second)
        .also { (state, commands) ->
            // non tab screens should always go before tab screens
            // there always should be at least 1 tab screen
        }
}

fun AppState.findTabScreenIndex(
    nav: TabNavigation,
): Int = screens.indexOfFirst { s -> nav.id == s.id }

// if we encounter any screen out of bottom bar screens, we just close the app;
// we pop the last screen in another case
fun AppState.pop() =
    // fixme refactor this bit
    if (screen is TabScreen && (screen as TabScreen).screens.isEmpty()) this command CloseApp
    else popScreen().noCommand()

fun AppState.pushArticleDetailsScreen(
    nav: NavigateToArticleDetails,
) = pushScreen(ArticleDetailsState(nav.id, nav.article)).noCommand()

private fun LoadArticlesByQuery(
    id: ScreenId,
    query: Query,
    resultsPerPage: Int = ArticlesState.ArticlesPerPage,
) = LoadArticlesByQuery(id, query, 0, resultsPerPage)
