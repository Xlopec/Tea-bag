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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app.feature.navigation

import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.Screen
import io.github.xlopec.reader.app.TabScreen
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.filter.FilterCommand
import io.github.xlopec.reader.app.feature.filter.FiltersInitialUpdate
import io.github.xlopec.reader.app.screen
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.navigation.NavigationStack
import io.github.xlopec.tea.navigation.mutate
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

public typealias AppNavigationStack = NavigationStack<Screen>

internal fun navigate(
    nav: Navigation,
    state: AppState,
    debug: Boolean = true,
): Update<AppState, Command> {
// gson serializer breaks singletons identity, thus we should rely on `is` check rather
// than referential equality
    return when (nav) {
        is TabNavigation -> state.navigateToTab(nav, TabNavigation::toTabScreen)
        is NavigateToArticleDetails -> state.navigateToArticleDetails(nav)
        is NavigateToFilters -> state.navigateToFilters(nav)
        is Pop -> state.popScreen()
    }.also { (appState, _) ->
        if (debug) {
            checkInvariants(appState)
        }
    }
}

internal fun AppState.navigateToTab(
    nav: TabNavigation,
    screenWithCommand: (TabNavigation) -> Update<TabScreen, Command>,
): Update<AppState, Command> {
    val i = findTabScreenIndex(nav.tab)

    val (screens, commands) = screens.mutate {
        if (i >= 0) {
            // tab might contain child screen stack
            // collect all child screens for this tab and place them on the top of the stack
            switchToTab(nav.tab, ::screenBelongsToTab)
        } else {
            push(screenWithCommand(nav))
        }
    }

    return copy(screens = screens) command commands
}

internal fun AppState.navigateToArticleDetails(
    nav: NavigateToArticleDetails,
): Update<AppState, Command> {
    val (screens, commands) = screens.mutate<_, _, Command> { push(ArticleDetailsState(nav.id, nav.article)) }

    return copy(screens = screens) command commands
}

internal fun AppState.navigateToFilters(
    nav: NavigateToFilters,
): Update<AppState, FilterCommand> {
    val (screens, commands) = screens.mutate { push(FiltersInitialUpdate(nav.id, nav.filter)) }

    return copy(screens = screens) command commands
}

internal fun AppState.findTabScreenIndex(
    tab: Tab,
): Int = screens.indexOfFirst { s -> tab.id == s.id }

internal fun AppState.popScreen(): Update<AppState, Command> {
    val screen = screen

    return if (screen is TabScreen) {
        noCommand()
    } else {
        val (screens, commands) = screens.mutate<_, _, Command> { mutator.removeLast() }
        copy(screens = screens) command commands
    }
}

private fun screenBelongsToTab(
    screen: Screen,
    tab: Tab,
): Boolean {
    return (screen as? TabScreen)?.id == tab.id
}

private fun checkInvariants(
    state: AppState,
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
