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

package com.max.reader.app.update

import com.max.reader.BuildConfig
import com.max.reader.app.*
import com.max.reader.app.message.*
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.max.reader.screens.article.list.QueryType.*
import com.max.reader.screens.settings.SettingsState
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

fun interface AppNavigation {

    fun navigate(
        nav: Navigation,
        state: AppState,
    ): UpdateWith<AppState, Command>
}

fun AppNavigation() = AppNavigation { nav, state ->
    when (nav) {
        // gson serializer breaks singletons identity, thus we should rely on `is` check rather
        // then referential equality
        is NavigateToFeed -> state.pushBottomNavigationScreen(nav, Query("android", Regular))
        is NavigateToFavorite -> state.pushBottomNavigationScreen(nav, Query("", Favorite))
        is NavigateToTrending -> state.pushBottomNavigationScreen(nav, Query("", Trending))
        is NavigateToArticleDetails -> state.pushArticleDetailsScreen(nav)
        is NavigateToSettings -> state.pushBottomNavigationScreenForSettings(nav)
        is Pop -> state.pop()
    }
}

// todo refactor, implement using another approach
// if we encounter any screen out of bottom bar screens, we just close the app;
// we pop the last screen in another case
fun AppState.pop() =
    if (screens.last().isBottomBarScreen) this command CloseApp
    else popScreen().noCommand()

fun AppState.pushArticleDetailsScreen(
    nav: NavigateToArticleDetails,
) = pushScreen(ArticleDetailsState(nav.id, nav.article)).noCommand()

fun AppState.pushBottomNavigationScreenForSettings(
    nav: NavigateToSettings,
): UpdateWith<AppState, Command> =
    pushScreenIfNotExistsWithState(nav) { SettingsState }
        .also { (newState, _) ->
            if (BuildConfig.DEBUG) {
                checkSettingsScreensNumber(newState, this)
            }
        }

inline fun AppState.pushScreenIfNotExistsWithState(
    nav: Navigation,
    crossinline screenWithCommand: () -> ScreenState,
): UpdateWith<AppState, Command> =
    pushScreenIfNotExists(nav) { screenWithCommand() to emptySet() }

inline fun AppState.pushScreenIfNotExists(
    nav: Navigation,
    crossinline screenWithCommand: (ScreenId) -> UpdateWith<ScreenState, Command>,
): UpdateWith<AppState, Command> {
    val i = findExistingScreenForNavigation(nav)
    val screenToCommand by lazy(LazyThreadSafetyMode.NONE) { screenWithCommand(nav.id) }
    val swap = i >= 0
    val newState = if (swap) {
        // move current screen to the end of screens stack,
        // so that it'll be popped out first
        swapWithLast(i)
    } else {
        pushScreen(screenToCommand.first)
    }

    return if (swap) newState.noCommand()
    else newState command screenToCommand.second
}

fun AppState.pushBottomNavigationScreen(
    nav: Navigation,
    query: Query,
): UpdateWith<AppState, Command> =
    pushScreenIfNotExists(nav) { screenId ->
        ArticlesState.newLoading(screenId, query) command LoadArticlesByQuery(screenId, query)
    }.also { (newState, _) ->
        if (BuildConfig.DEBUG) {
            checkArticlesScreensNumber(nav, query, newState, this)
        }
    }

fun AppState.findExistingScreenForNavigation(
    nav: Navigation,
): Int = screens.indexOfFirst { s -> nav.id == s.id }

private inline val ScreenState.isBottomBarScreen: Boolean
    get() = this is ArticlesState || this is SettingsState

private fun checkArticlesScreensNumber(
    nav: Navigation,
    query: Query,
    newState: AppState,
    oldState: AppState,
) {
    val grouped = newState.screens.groupBy { screen -> (screen as? ArticlesState)?.query?.type }

    val predicate =
        QueryType.values().map { type -> grouped[type]?.size ?: 0 }.all { size -> size <= 1 }

    check(predicate) {
        WrongScreensNumberErrorMessage(nav, query, newState, oldState)
    }
}

private fun checkSettingsScreensNumber(
    newState: AppState,
    oldState: AppState,
) =
    check(newState.screens.count { screen -> screen is SettingsState } == 1) {
        WrongSettingsScreensNumberErrorMessage(newState, oldState)
    }

private fun WrongSettingsScreensNumberErrorMessage(
    newState: AppState,
    oldState: AppState,
) = "Wrong number of settings screens after update," +
        "new state: $newState,\nold state: $oldState"

private fun WrongScreensNumberErrorMessage(
    nav: Navigation,
    query: Query,
    newState: AppState,
    oldState: AppState,
) = "Wrong number of bottom navigation screens after navigation update: " +
        "$nav,\nquery: $query,\nnew state: $newState,\nold state: $oldState"

private fun LoadArticlesByQuery(
    id: ScreenId,
    query: Query,
    resultsPerPage: Int = ArticlesState.ArticlesPerPage,
) = LoadArticlesByQuery(id, query, 0, resultsPerPage)
