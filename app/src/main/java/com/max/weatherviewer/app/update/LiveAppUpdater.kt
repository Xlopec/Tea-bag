@file:Suppress("FunctionName")

package com.max.weatherviewer.app.update

import com.max.weatherviewer.app.CloseApp
import com.max.weatherviewer.app.Command
import com.max.weatherviewer.app.LoadByCriteria
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.NavigateToFavorite
import com.max.weatherviewer.app.NavigateToFeed
import com.max.weatherviewer.app.NavigateToTrending
import com.max.weatherviewer.app.Navigation
import com.max.weatherviewer.app.Pop
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.app.ScreenMessage
import com.max.weatherviewer.app.State
import com.max.weatherviewer.app.popScreen
import com.max.weatherviewer.app.pushScreen
import com.max.weatherviewer.app.updateScreen
import com.max.weatherviewer.screens.feed.ArticleUpdated
import com.max.weatherviewer.screens.feed.ArticlesLoaded
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.feed.FeedLoading
import com.max.weatherviewer.screens.feed.FeedMessage
import com.max.weatherviewer.screens.feed.FeedOperationException
import com.max.weatherviewer.screens.feed.LoadArticles
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.max.weatherviewer.screens.feed.OnQueryUpdated
import com.max.weatherviewer.screens.feed.OpenArticle
import com.max.weatherviewer.screens.feed.ShareArticle
import com.max.weatherviewer.screens.feed.ToggleArticleIsFavorite
import com.max.weatherviewer.screens.feed.update.FeedUpdater
import com.max.weatherviewer.screens.feed.update.LiveFeedUpdater.update
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import java.util.*

fun <Env> AppUpdater(): AppUpdater<Env> where Env : FeedUpdater = object :
        LiveAppUpdater<Env> {}

interface LiveAppUpdater<Env> : AppUpdater<Env> where Env : FeedUpdater {

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
            is FeedMessage -> state.updateScreen<Feed>(message.id) { feed -> update(message, feed) }
            else -> error("Unknown screen message, was $message")
        }

    fun navigate(
        nav: Navigation,
        state: State
    ): UpdateWith<State, Command> {

        val screens = state.screens.size

        return when {
            nav is NavigateToFeed -> state.pushScreen(nav, LoadCriteria.Query("android"))
            nav is NavigateToFavorite -> state.pushScreen(nav, LoadCriteria.Favorite)
            nav is NavigateToTrending -> state.pushScreen(nav, LoadCriteria.Trending)
            nav is Pop && screens > 1 -> state.popScreen().noCommand()
            nav is Pop && screens == 1 -> state command CloseApp
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

    fun State.pushScreen(
        nav: Navigation,
        loadCriteria: LoadCriteria
    ): UpdateWith<State, Command> {

        val i = findExistingScreen(nav)

        return if (i >= 0) {
            pushScreen(screens[i]).noCommand()
        } else {
            val id = UUID.randomUUID()
            pushScreen(
                FeedLoading(
                    id,
                    loadCriteria
                )
            ) command LoadByCriteria(
                id,
                loadCriteria
            )
        }

    }

    fun State.pushFeedScreen(
        criteria: LoadCriteria
    ): UpdateWith<State, LoadByCriteria> {
        val id = UUID.randomUUID()

        return pushScreen(
            FeedLoading(
                id,
                criteria
            )
        ) command LoadByCriteria(
            id,
            criteria
        )
    }

    val FeedMessage.id: ScreenId?
        get() = when (this) {
            is LoadArticles -> id
            is ToggleArticleIsFavorite -> id
            is ArticlesLoaded -> id
            is FeedOperationException -> id
            is OnQueryUpdated -> id
            is ArticleUpdated, is OpenArticle, is ShareArticle -> null
        }

}
