@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.max.weatherviewer.CloseApp
import com.max.weatherviewer.Command
import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.home.*
import com.max.weatherviewer.home.LiveHomeUpdater.update
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

interface AppUpdater<Env> {

    fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command>

}

fun <Env> AppUpdater(): AppUpdater<Env> where Env : HomeUpdater = object : LiveAppUpdater<Env> {}

interface LiveAppUpdater<Env> : AppUpdater<Env> where Env : HomeUpdater {

    override fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> updateScreen(message, state)
        }

    fun updateScreen(
        message: ScreenMessage,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is HomeMessage -> state.updateScreen<Feed> { home -> update(message, home) }
            else -> error("Unhandled screen message $message for state $state")
        }

    fun navigate(
        nav: Navigation,
        state: State
    ): UpdateWith<State, Command> {

        val existingIndex = state.findExistingScreen(nav)
        val screens = state.screens.size

        return when {
            existingIndex >= 0 -> state.swapScreens(existingIndex).noCommand()
            nav is NavigateToFeed -> state.pushFeedScreen(LoadCriteria.Query(""))
            nav is NavigateToFavorite -> state.pushFeedScreen(LoadCriteria.Favorite)
            nav is NavigateToTranding -> state.pushFeedScreen(LoadCriteria.Trending)
            nav === Pop && screens > 1 -> state.popScreen().noCommand()
            nav === Pop && screens == 1 -> state command CloseApp
            else -> error("Unexpected state")
        }
    }

    fun State.findExistingScreen(
        nav: Navigation
    ): Int = screens.indexOfFirst { s ->
        s is Feed && isCriteriaMatches(s.criteria, nav)
    }

    fun isCriteriaMatches(
        criteria: LoadCriteria,
        nav: Navigation
    ) = (criteria is LoadCriteria.Query && nav === NavigateToFeed)
        || (criteria == LoadCriteria.Trending && nav === NavigateToTranding)
        || (criteria == LoadCriteria.Favorite && nav === NavigateToFavorite)

    fun State.pushFeedScreen(
        criteria: LoadCriteria
    ) = pushScreen(FeedLoading(criteria)) command LoadByCriteria(criteria)

}
