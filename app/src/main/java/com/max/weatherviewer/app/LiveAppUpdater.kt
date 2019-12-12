@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.max.weatherviewer.CloseApp
import com.max.weatherviewer.Command
import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.home.*
import com.max.weatherviewer.home.LiveFeedUpdater.update
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import java.util.*

interface AppUpdater<Env> {

    fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command>

}

fun <Env> AppUpdater(): AppUpdater<Env> where Env : FeedUpdater = object : LiveAppUpdater<Env> {}

interface LiveAppUpdater<Env> : AppUpdater<Env> where Env : FeedUpdater {

    override fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessageWrapper -> updateScreen(message.message, state)
        }

    fun updateScreen(
        message: ScreenMessage,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is FeedMessage -> state.updateScreen<Feed>(message.id) { feed -> update(message, feed) }
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
            nav is NavigateToTrending -> state.pushFeedScreen(LoadCriteria.Trending)
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
        || (criteria == LoadCriteria.Trending && nav === NavigateToTrending)
        || (criteria == LoadCriteria.Favorite && nav === NavigateToFavorite)

    fun State.pushFeedScreen(
        criteria: LoadCriteria
    ): UpdateWith<State, LoadByCriteria> {
        val id = UUID.randomUUID()

        return pushScreen(FeedLoading(id, criteria)) command LoadByCriteria(id, criteria)
    }

    val ScreenMessage.id: ScreenId?
        get() = when (this) {
            is LoadArticles -> id
            is ToggleArticleIsFavorite -> id
            is ArticlesLoaded -> id
            is ArticlesLoadException -> id
            is ArticleUpdated -> null
        }

}
