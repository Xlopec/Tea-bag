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
import com.oliynick.max.reader.article.list.Paging.Companion.FirstPage
import com.oliynick.max.reader.article.list.Query
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.settings.SettingsState
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

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
    }.also { (appState, _) ->
        if (debug) {
            checkInvariants(appState)
        }
    }
}

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

/**
 * This functions moves specified navigation group to the top of the navigation stack and returns
 * new navigation stack
 *
 * r1 - root #1
 *
 * s1,2 - screen #1 the belongs to the root #2
 *
 * s3 - screen #3 that should be drawn fullscreen. Fullscreen objects must be placed on the top,
 * they can't reside in the middle of the stack. You can't navigate from fullscreen to a nested
 * screen since it complicates navigation from user perspective, e.g. consider case when navigating
 * from fullscreen #1 that resides in tab #2 to tab #3 and then navigating back to the tab #1.
 *
 * Screens that relate to the same root are considered to belong to the same stack group.
 * Screens that should be rendered simultaneously are considered to belong to the same drawing group
 * At any moment only one drawing group can be rendered
 *
 * Navigation stack sample:
 *
 * ```
 * s2,2 <- screen to draw
 * s1,2 <- screen to draw
 * r2 <- drawing frame, end of group
 * r1
 * s4,1
 * s5,1
 * ...
 * ```
 *
 * Will produce the following result:
 *
 * ```
 * +-----------------------------+
 * |         Root 2 (r2)         |
 * |   +---------------------+   |
 * |   |   Screen 2 (s2,2)   |   |
 * |   | +-----------------+ |   |
 * |   | | Screen 1 (s1,2) | |   |
 * |   | +-----------------+ |   |
 * |   +---------------------+   |
 * +-----------------------------+
 * ```
 *
 * Navigation stack for screen that occupies whole display:
 *
 * ```
 * s100 <- fullscreen
 * r2
 * s3,2
 * s2,2
 * s1,2
 * r1
 * s4,1
 * s5,1
 * ...
 * ```
 *
 * In this case no drawing frame will be displayed
 *
 * ***Implementation note***. If we ever have to support nesting of the depth more than 1, drawing
 * groups might be implemented as linked list.
 */
fun NavigationStack.floatGroup(
    tabIdx: Int,
    tabId: ScreenId
): NavigationStack {
    require(tabIdx in indices) { "Tab index out of bounds, bounds=${indices}, index=$tabIdx" }

    var bottomGroupIdx = 0

    return foldRightIndexed(this) { idx, screen, acc ->
        // we don't support nesting of the depth more than 1,
        // reference to the tab is enough for now
        if ((screen as? NestedScreen)?.tabId == tabId) {
            bottomGroupIdx++
            acc.swap(idx, 0)
        } else {
            acc
        }
    }.swap(tabIdx, bottomGroupIdx)
}

fun AppState.findTabScreenIndex(
    nav: TabNavigation,
): Int = screens.indexOfFirst { s -> nav.id == s.id }

val AppState.currentTab: TabScreen
    get() = screens.first { it is TabScreen } as TabScreen

fun AppState.navigateToArticleDetails(
    nav: NavigateToArticleDetails,
) = pushScreen(ArticleDetailsState(nav.id, nav.article)).noCommand()
