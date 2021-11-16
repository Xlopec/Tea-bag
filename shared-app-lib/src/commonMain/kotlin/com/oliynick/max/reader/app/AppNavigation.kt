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

import com.oliynick.max.reader.article.details.ArticleDetailsState
import com.oliynick.max.reader.article.list.ArticlesState
import com.oliynick.max.reader.article.list.Query
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.settings.SettingsState
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.PersistentList

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
        is TabNavigation -> state.navigateToTab(nav, TabNavigation::toTabScreen)
        // gson serializer breaks singletons identity, thus we should rely on `is` check rather
        // then referential equality
        is NavigateToArticleDetails -> state.navigateToArticleDetails(nav)
        is Pop -> state.popScreen()
    }
}

// todo refactor later
fun TabNavigation.toTabScreen(): UpdateWith<TabScreen, Command> =
    when (this) {
        NavigateToSettings -> SettingsState.noCommand()
        NavigateToFeed -> ArticlesInitialUpdate(id, Query("android", Regular))
        NavigateToFavorite -> ArticlesInitialUpdate(id, Query("", Favorite))
        NavigateToTrending -> ArticlesInitialUpdate(id, Query("", Trending))
    }

private fun ArticlesInitialUpdate(
    id: ScreenId,
    query: Query
) = ArticlesState.newLoading(id, query) command LoadArticlesByQuery(id, query)

fun AppState.navigateToTab(
    nav: TabNavigation,
    screenWithCommand: (TabNavigation) -> UpdateWith<TabScreen, Command>,
): UpdateWith<AppState, Command> {
    val i = findTabScreenIndex(nav)

    return if (i >= 0) {
        // tab might contain child screen stack
        // collect all child screens for this tab and place them on the top of the stack
        copy(screens = screens.swapGroups(i, nav.id)).noCommand()
    } else {

        val (tab, cmds) = screenWithCommand(nav)

        pushScreen(tab) command cmds
    }
}

fun <T> PersistentList<T>.swapGroups(
    tabIdx: Int,
    tabId: ScreenId
): PersistentList<T> {
    require(tabIdx in indices) { "Tab index out of bounds, bounds=${indices}, index=$tabIdx" }

    var newList = this
    var bottomGroupIdx = 0

    indices.reversed().forEach { idx ->
        if ((this[idx] as? FullScreen)?.tabId == tabId) {
            newList = swap(idx, 0)
            bottomGroupIdx++
        }
    }

    return newList.swap(tabIdx, bottomGroupIdx)
}

fun AppState.findTabScreenIndex(
    nav: TabNavigation,
): Int = screens.indexOfFirst { s -> nav.id == s.id }

private val AppState.currentTab: TabScreen
    get() = screens.first { it is TabScreen } as TabScreen

fun AppState.navigateToArticleDetails(
    nav: NavigateToArticleDetails,
) = pushScreen(ArticleDetailsState(nav.id, nav.article, currentTab.id)).noCommand()

private fun LoadArticlesByQuery(
    id: ScreenId,
    query: Query,
    resultsPerPage: Int = ArticlesState.ArticlesPerPage,
) = LoadArticlesByQuery(id, query, 0, resultsPerPage)
