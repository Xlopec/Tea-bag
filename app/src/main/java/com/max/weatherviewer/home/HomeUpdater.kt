package com.max.weatherviewer.home

import com.max.weatherviewer.Command
import com.max.weatherviewer.DoLoadArticles
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

object HomeUpdater {

    fun update(message: HomeMessage, home: Home): UpdateWith<Home, Command> {
        return when (message) {
            is ArticlesLoaded -> Preview(message.articles).noCommand()
            is LoadArticles -> Loading command DoLoadArticles(message.query)
            is ArticlesLoadException -> Error(message.cause).noCommand()
        }
    }

}