package com.max.weatherviewer.home

import com.max.weatherviewer.DoLoadArticles
import com.max.weatherviewer.HomeCommand
import com.max.weatherviewer.NewsApi
import com.max.weatherviewer.app.Message
import com.oliynick.max.elm.core.component.effect

object HomeResolver {

    suspend fun resolve(dependencies: HomeDependencies, command: HomeCommand): Set<Message> {

        suspend fun resolve(command: HomeCommand) =
            dependencies.run {
                when (command) {
                    is DoLoadArticles -> command.effect {
                        ArticlesLoaded(
                            newsApi(command.query)
                        )
                    }
                }
            }

        return runCatching { resolve(command) }
            .getOrElse { th -> setOf(
                ArticlesLoadException(
                    "bitcoin",
                    th
                )
            ) }
    }

}

data class HomeDependencies(
    val newsApi: NewsApi
)