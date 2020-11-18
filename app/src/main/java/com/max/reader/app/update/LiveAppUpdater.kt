@file:Suppress("FunctionName")

package com.max.reader.app.update

import com.max.reader.app.*
import com.max.reader.screens.article.details.ArticleDetailsMessage
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.OpenInBrowser
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.list.*
import com.max.reader.screens.article.list.update.ArticlesUpdater
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import java.util.*

fun <Env> AppUpdater(): AppUpdater<Env> where Env : ArticlesUpdater, Env : ArticleDetailsUpdater =
    object : LiveAppUpdater<Env> {}

// todo remove uuid generation from here
interface LiveAppUpdater<Env> : AppUpdater<Env> where Env : ArticlesUpdater,
                                                      Env : ArticleDetailsUpdater {

    override fun Env.update(
        message: Message,
        state: AppState,
    ): UpdateWith<AppState, Command> =
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> updateScreen(message, state)
        }

    fun Env.updateScreen(
        message: ScreenMessage,
        state: AppState,
    ): UpdateWith<AppState, Command> =
        when (message) {
            is ArticlesMessage -> state.updateScreen<ArticlesState>(message.id) { screen -> update(message, screen) }
            is ArticleDetailsMessage -> state.updateScreen<ArticleDetailsState>(message.id) { screen -> update(message, screen) }
            else -> error("Unknown screen message, was $message")
        }

    fun navigate(
        nav: Navigation,
        state: AppState,
    ): UpdateWith<AppState, Command> =
        when (nav) {
            is NavigateToFeed -> state.pushBottomNavigationScreen(nav, LoadCriteria.Query("android"))
            is NavigateToFavorite -> state.pushBottomNavigationScreen(nav, LoadCriteria.Favorite)
            is NavigateToTrending -> state.pushBottomNavigationScreen(nav, LoadCriteria.Trending)
            is NavigateToArticleDetails -> state.pushArticleDetailsScreen(nav)
            // simply close the app
            is Pop -> state.pop()
        }

    fun AppState.pop() =
        if (screens.last() is ArticlesState) this command CloseApp
        else popScreen().noCommand()

    fun AppState.pushArticleDetailsScreen(
        nav: NavigateToArticleDetails,
    ) = pushScreen(ArticleDetailsState(UUID.randomUUID(), nav.article)).noCommand()

    fun AppState.pushBottomNavigationScreen(
        nav: Navigation,
        loadCriteria: LoadCriteria,
    ): UpdateWith<AppState, Command> {

        val i = findExistingArticlesScreenForNavigation(nav)

        return if (i >= 0) {
            // move current screen to the end of screens stack,
            // so that it'll be popped out first
            swapScreens(i, screens.lastIndex).noCommand()
        } else {
            val id = UUID.randomUUID()
            pushScreen(
                ArticlesLoadingState(
                    id,
                    loadCriteria
                )
            ) command LoadByCriteria(
                id,
                loadCriteria
            )
        }
    }

    fun AppState.findExistingArticlesScreenForNavigation(
        nav: Navigation,
    ): Int = screens.indexOfFirst { s ->
        s is ArticlesState && isCriteriaMatches(s.criteria, nav)
    }

    fun isCriteriaMatches(
        criteria: LoadCriteria,
        nav: Navigation,
    ) = (criteria is LoadCriteria.Query && nav === NavigateToFeed)
            || (criteria == LoadCriteria.Trending && nav === NavigateToTrending)
            || (criteria == LoadCriteria.Favorite && nav === NavigateToFavorite)

    val ArticlesMessage.id: ScreenId?
        get() = when (this) {
            is LoadArticles -> id
            is ToggleArticleIsFavorite -> id
            is ArticlesLoaded -> id
            is ArticlesOperationException -> id
            is OnQueryUpdated -> id
            is ArticleUpdated, is ShareArticle -> null
        }

    val ArticleDetailsMessage.id: ScreenId
        get() = when (this) {
            is OpenInBrowser -> id
        }

}
