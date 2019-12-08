@file:Suppress("FunctionName")

package com.max.weatherviewer.home

import com.max.weatherviewer.DoLoadArticles
import com.max.weatherviewer.HomeCommand
import com.max.weatherviewer.NewsApi
import com.max.weatherviewer.app.Message
import com.oliynick.max.elm.core.component.effect
import retrofit2.Retrofit

interface HomeResolver<Env> {

    suspend fun Env.resolve(command: HomeCommand): Set<Message>

}

fun <Env> HomeResolver(): HomeResolver<Env> where Env : HasNewsApi =
    object : LiveHomeResolver<Env> {}

interface LiveHomeResolver<Env> : HomeResolver<Env> where Env : HasNewsApi {

    override suspend fun Env.resolve(command: HomeCommand): Set<Message> {
        suspend fun resolve(command: HomeCommand) =
                when (command) {
                    is DoLoadArticles -> command.effect {
                        ArticlesLoaded(
                            newsApi(command.query)
                        )
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

interface HasNewsApi {
    val newsApi: NewsApi
}

fun NewsApi(retrofit: Retrofit): HasNewsApi = object : HasNewsApi {
    override val newsApi: NewsApi by lazy { NewsApi(retrofit) }
}
