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

package com.oliynick.max.reader.app.feature.navigation

import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsState
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.article.list.LoadArticlesByQuery
import com.oliynick.max.reader.app.feature.article.list.Paging.Companion.FirstPage
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.article.list.QueryType.*
import com.oliynick.max.reader.app.feature.settings.SettingsScreen
import com.oliynick.max.reader.app.feature.suggest.SuggestState
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

fun navigate(
    nav: Navigation,
    state: AppState,
    debug: Boolean = true
): UpdateWith<AppState, Command> {
// gson serializer breaks singletons identity, thus we should rely on `is` check rather
// then referential equality
    return when (nav) {
        is TabNavigation -> state.navigateToTab(nav, TabNavigation::toTabScreen)
        is NavigateToArticleDetails -> state.navigateToArticleDetails(nav)
        is NavigateToSuggestions -> state.navigateToSuggestions(nav)
        is Pop -> state.popScreen()
    }.also { (appState, _) ->
        if (debug) {
            checkInvariants(appState)
        }
    }
}

// todo refactor later
fun TabNavigation.toTabScreen(): UpdateWith<TabScreen, Command> =
    when (this) {
        NavigateToSettings -> SettingsScreen.noCommand()
        NavigateToFeed -> ArticlesInitialUpdate(id, Query("android", Regular))
        NavigateToFavorite -> ArticlesInitialUpdate(id, Query("", Favorite))
        NavigateToTrending -> ArticlesInitialUpdate(id, Query("", Trending))
    }

private fun ArticlesInitialUpdate(
    id: ScreenId,
    query: Query
) = ArticlesState.newLoading(id, query) command LoadArticlesByQuery(id, query, FirstPage)

fun AppState.navigateToTab(
    nav: TabNavigation,
    screenWithCommand: (TabNavigation) -> UpdateWith<TabScreen, Command>,
): UpdateWith<AppState, Command> {
    val i = findTabScreenIndex(nav)

    return if (i >= 0) {
        // tab might contain child screen stack
        // collect all child screens for this tab and place them on the top of the stack
        copy(screens = screens.floatGroup(i, nav.id)).noCommand()
    } else {

        val (tab, cmds) = screenWithCommand(nav)

        pushScreen(tab) command cmds
    }
}

fun AppState.findTabScreenIndex(
    nav: TabNavigation,
): Int = screens.indexOfFirst { s -> nav.id == s.id }

val AppState.currentTab: TabScreen
    get() = screens.first { it is TabScreen } as TabScreen

fun AppState.navigateToArticleDetails(
    nav: NavigateToArticleDetails,
) = pushScreen(ArticleDetailsState(nav.id, nav.article)).noCommand()

fun AppState.navigateToSuggestions(
    nav: NavigateToSuggestions
) = pushScreen(SuggestState(nav.id, nav.query)).noCommand()

expect fun AppState.popScreen(): UpdateWith<AppState, Command>

// todo looks like we should extract class for navigation stack
private fun checkInvariants(
    state: AppState
) {
    check(state.screens.size > 0) { "screens stack can't be empty" }
    check(state.screens.count { it is TabScreen } > 0) {
        "should be at least one tab screen, were ${state.screens}"
    }

    val mapping = state.screens.groupBy({ it::class }, state.screens::indexOf)
    val fullScreens = mapping[FullScreen::class]
    // maximum fullscreen index should be less than minimum index for any other screen type
    val maxFullScreenIndex = fullScreens?.maxOrNull() ?: MIN_VALUE
    val maxOtherIndex =
        mapping.filterKeys { it != FullScreen::class }.values.flatten().minOrNull() ?: MAX_VALUE

    check(maxFullScreenIndex < maxOtherIndex) {
        "maxFullScreenIndex >= maxOtherIndex, was ${state.screens}"
    }
    // fullscreen segment should be continuous
    check(fullScreens?.let { it.indices == it.first()..it.last() } ?: true) {
        "Fullscreen segment isn't continuous, was $fullScreens for stack ${state.screens}"
    }
}