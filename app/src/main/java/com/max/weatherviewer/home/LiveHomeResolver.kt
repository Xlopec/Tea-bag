@file:Suppress("FunctionName")

package com.max.weatherviewer.home

import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.NewsApi
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.ScreenMsg
import com.max.weatherviewer.persistence.Storage
import com.oliynick.max.elm.core.component.effect
import retrofit2.Retrofit

interface HomeResolver<Env> {

    suspend fun Env.resolve(command: LoadByCriteria): Set<Message>

}

fun <Env> HomeResolver(): HomeResolver<Env> where Env : HasNewsApi, Env : Storage<Env> =
    object : LiveHomeResolver<Env> {}

interface LiveHomeResolver<Env> : HomeResolver<Env> where Env : HasNewsApi, Env : Storage<Env> {

    override suspend fun Env.resolve(command: LoadByCriteria): Set<ScreenMsg> {
        suspend fun resolve() =
            when (command.criteria) {
                is LoadCriteria.Query -> command.effect {
                    ScreenMsg(
                        ArticlesLoaded(
                            id,
                            newsApi(command.criteria.query)
                        )
                    )
                }
                LoadCriteria.Favorite -> command.effect {

                    val articles = newsApi("android")

                    addToFavorite(articles.first())

                    val fav = getFavorite()

                    println(fav)

                    ScreenMsg(
                        ArticlesLoaded(
                            id,
                            articles
                        )
                    )
                }
                LoadCriteria.Trending -> command.effect {
                    ScreenMsg(
                        ArticlesLoaded(
                            id,
                            newsApi("bitcoin")
                        )
                    )
                }
            }


        return runCatching { resolve() }
            .getOrElse { th -> setOf(ScreenMsg(ArticlesLoadException(command.id, th))) }
    }
}

interface HasNewsApi {
    val newsApi: NewsApi
}

fun NewsApi(retrofit: Retrofit): HasNewsApi = object : HasNewsApi {
    override val newsApi: NewsApi by lazy { NewsApi(retrofit) }
}
