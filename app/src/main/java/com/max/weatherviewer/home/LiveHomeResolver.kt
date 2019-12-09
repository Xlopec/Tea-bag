@file:Suppress("FunctionName")

package com.max.weatherviewer.home

import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.NewsApi
import com.max.weatherviewer.app.Message
import com.oliynick.max.elm.core.component.effect
import retrofit2.Retrofit

interface HomeResolver<Env> {

    suspend fun Env.resolve(command: LoadByCriteria): Set<Message>

}

fun <Env> HomeResolver(): HomeResolver<Env> where Env : HasNewsApi =
    object : LiveHomeResolver<Env> {}

interface LiveHomeResolver<Env> : HomeResolver<Env> where Env : HasNewsApi {

    override suspend fun Env.resolve(command: LoadByCriteria): Set<Message> {
        suspend fun resolve() =
            when (command.criteria) {
                is LoadCriteria.Query -> command.effect {
                    ArticlesLoaded(
                        newsApi(command.criteria.query)
                    )
                }
                LoadCriteria.Favorite -> command.effect {
                    ArticlesLoaded(
                        newsApi("android")
                    )
                }
                LoadCriteria.Trending -> command.effect {
                    ArticlesLoaded(
                        newsApi("bitcoin")
                    )
                }
            }


        return runCatching { resolve() }
            .getOrElse { th -> setOf(ArticlesLoadException(th)) }
    }
}

interface HasNewsApi {
    val newsApi: NewsApi
}

fun NewsApi(retrofit: Retrofit): HasNewsApi = object : HasNewsApi {
    override val newsApi: NewsApi by lazy { NewsApi(retrofit) }
}
