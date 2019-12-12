@file:Suppress("FunctionName")

package com.max.weatherviewer.home

import com.max.weatherviewer.FeedCommand
import com.max.weatherviewer.LoadByCriteria
import com.max.weatherviewer.NewsApi
import com.max.weatherviewer.RemoveArticle
import com.max.weatherviewer.SaveArticle
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.app.ScreenMessageWrapper
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.persistence.Storage
import com.oliynick.max.elm.core.component.effect
import retrofit2.Retrofit

interface FeedResolver<Env> {

    suspend fun Env.resolve(command: FeedCommand): Set<Message>

}

fun <Env> HomeResolver(): FeedResolver<Env> where Env : HasNewsApi, Env : Storage<Env> =
    object : LiveFeedResolver<Env> {}

interface LiveFeedResolver<Env> : FeedResolver<Env> where Env : HasNewsApi, Env : Storage<Env> {

    override suspend fun Env.resolve(command: FeedCommand): Set<ScreenMessageWrapper> {

        suspend fun resolve() =
            when (command) {
                is LoadByCriteria -> fetch(command.id, command.criteria)
                is SaveArticle -> store(command.article)
                is RemoveArticle -> remove(command.article)
            }


        return runCatching { resolve() }
            .getOrThrow()//rElse { th -> setOf(ScreenMessageWrapper(ArticlesLoadException(command.id, th))) }
    }

    suspend fun Env.store(
        article: Article
    ): Set<ScreenMessageWrapper> = effect { addToFavorite(article); ScreenMessageWrapper(ArticleUpdated(article)) }

    suspend fun Env.remove(
        article: Article
    ): Set<ScreenMessageWrapper> = effect { removeFromFavorite(article.url); ScreenMessageWrapper(ArticleUpdated(article)) }

    suspend fun Env.fetch(
        id: ScreenId,
        criteria: LoadCriteria
    ): Set<ScreenMessageWrapper> = when (criteria) {
            is LoadCriteria.Query -> criteria.effect {
                ScreenMessageWrapper(
                    ArticlesLoaded(
                        id,
                        newsApi(criteria.query).map {
                            it.copy(isFavorite = isFavorite(it.url))
                        }
                    )
                )
            }
            LoadCriteria.Favorite -> criteria.effect {
                ScreenMessageWrapper(
                    ArticlesLoaded(
                        id,
                        getFavorite()
                    )
                )
            }
            LoadCriteria.Trending -> criteria.effect {
                ScreenMessageWrapper(
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
