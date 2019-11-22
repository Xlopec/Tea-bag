package com.max.weatherviewer

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

sealed class HomeState

object Loading : HomeState()

data class Preview(val articles: List<Article>) : HomeState()

object HomeReducer {

    fun update(message: HomeMessage, state: State): UpdateWith<State, Command> {

        val index = state.screens.indexOfFirst { it is Home }

        if (index < 0) {
            return state.noCommand()
        }

        return when(message) {
            is ArticlesLoaded -> state.copy(screens = state.screens.set(index, Home(Preview(message.articles)))).noCommand()
            is LoadArticles -> state.copy(screens = state.screens.set(index, Home(Loading))) command DoLoadArticles(message.query)
        }
    }

}