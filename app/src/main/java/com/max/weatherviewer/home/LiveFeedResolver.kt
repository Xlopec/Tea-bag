@file:Suppress("FunctionName")

package com.max.weatherviewer.home

import com.max.weatherviewer.FeedCommand
import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.NewsApi
import com.max.weatherviewer.RemoveArticle
import com.max.weatherviewer.SaveArticle
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.app.ScreenMsg
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.persistence.Storage
import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.sideEffect
import retrofit2.Retrofit
import java.net.URL

interface FeedResolver<Env> {

    suspend fun Env.resolve(command: FeedCommand): Set<Message>

}

fun <Env> HomeResolver(): FeedResolver<Env> where Env : HasNewsApi, Env : Storage<Env> =
    object : LiveFeedResolver<Env> {}

interface LiveFeedResolver<Env> : FeedResolver<Env> where Env : HasNewsApi, Env : Storage<Env> {

    override suspend fun Env.resolve(command: FeedCommand): Set<ScreenMsg> {

        suspend fun resolve() =
            //fixme add NotifyArticleUpdatedMessage
            when (command) {
                is LoadByCriteria -> fetch(command.id, command.criteria)
                is SaveArticle -> store(command.article)
                is RemoveArticle -> remove(command.url)
            }


        return runCatching { resolve() }
            .getOrElse { th -> setOf(ScreenMsg(ArticlesLoadException(command.id, th))) }
    }

    suspend fun Env.store(
        article: Article
    ): Set<ScreenMsg> = sideEffect { addToFavorite(article) }

    suspend fun Env.remove(
        url: URL
    ): Set<ScreenMsg> = sideEffect { removeFromFavorite(url) }

    suspend fun Env.fetch(
        id: ScreenId,
        criteria: LoadCriteria
    ): Set<ScreenMsg> = when (criteria) {
            is LoadCriteria.Query -> criteria.effect {
                ScreenMsg(
                    ArticlesLoaded(
                        id,
                        newsApi(criteria.query).map {
                            it.copy(isFavorite = isFavorite(it.url))
                        }
                    )
                )
            }
            LoadCriteria.Favorite -> criteria.effect {
                ScreenMsg(
                    ArticlesLoaded(
                        id,
                        getFavorite()
                    )
                )
            }
            LoadCriteria.Trending -> criteria.effect {
                ScreenMsg(
                    ArticlesLoaded(
                        id,
                        // todo: what articles are considered to be trending?
                        newsApi("bitcoin").map {
                            it.copy(isFavorite = isFavorite(it.url))
                        }
                    )
                )
            }
        }

}

interface HasNewsApi {
    val newsApi: NewsApi
}

fun NewsApi(retrofit: Retrofit): HasNewsApi = object : HasNewsApi {
    override val newsApi: NewsApi by lazy { NewsApi(retrofit) }
}
