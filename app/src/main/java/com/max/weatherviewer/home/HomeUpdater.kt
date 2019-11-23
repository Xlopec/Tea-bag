package com.max.weatherviewer.home

import com.max.weatherviewer.Command
import com.max.weatherviewer.DoLoadArticles
import com.max.weatherviewer.app.*
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

object HomeUpdater {

    fun update(message: HomeMessage, home: Home): UpdateWith<Home, Command> {
        return home.run {
            when (message) {
                is ArticlesLoaded -> copy(state = Preview(message.articles)).noCommand()
                is LoadArticles -> copy(state = Loading) command DoLoadArticles(message.query)
                is ArticlesLoadException -> copy(state = Error(message.cause)).noCommand()
            }
        }
    }

}