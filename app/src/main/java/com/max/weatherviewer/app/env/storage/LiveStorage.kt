@file:Suppress("FunctionName")

package com.max.weatherviewer.app.env.storage

import android.app.Application
import com.max.weatherviewer.app.env.HasAppContext
import com.max.weatherviewer.app.env.storage.local.HasMongoCollection
import com.max.weatherviewer.app.env.storage.network.*
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.mongodb.client.model.*
import kotlinx.coroutines.*
import org.bson.Document
import java.net.URL

fun <Env> Storage(): Storage<Env> where Env : HasMongoCollection,
                                        Env : HasNewsApi,
                                        Env : HasAppContext,
                                        Env : HasGson = object : LiveStorage<Env> {
}

interface LiveStorage<Env> : Storage<Env> where Env : HasMongoCollection,
                                                Env : HasNewsApi,
                                                Env : HasAppContext,
                                                Env : HasGson {

    private companion object {
        const val API_KEY = "08a7e13902bf4cffab115365071e3850"
    }

    override suspend fun Env.addToFavorite(article: Article) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.replaceOne(
                    Filters.eq("url", article.url.toExternalForm()),
                    Document.parse(gson.toJson(article)),
                    ReplaceOptions.createReplaceOptions(UpdateOptions().upsert(true))
                )
            }
        }
    }

    override suspend fun Env.removeFromFavorite(url: URL) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.findOneAndDelete(
                    Filters.eq("url", url.toExternalForm())
                )
            }
        }
    }

    override suspend fun Env.fetchFavorite(): List<Article> =
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection
                    .find()
                    .map { gson.fromJson(it.toJson(), Article::class.java) }
                    .into(ArrayList())
            }
        }

    override suspend fun Env.fetch(criteria: LoadCriteria.Query): List<Article> =
        toArticles(api.fetchFromEverything(API_KEY, mapOf("q" to criteria.query))).take(2)

    override suspend fun Env.fetchTrending(): List<Article> =
        toArticles(api.fetchTopHeadlines(API_KEY, application.countryCode))

    private suspend fun Env.toArticles(response: ArticleResponse) =
        response.articles.map { elem -> toArticle(elem) }

    private suspend fun Env.toArticle(element: ArticleElement) =
        Article(
            url = element.url,
            title = element.title,
            author = element.author,
            description = element.description,
            urlToImage = element.urlToImage,
            isFavorite = isFavorite(element.url),
            published = element.publishedAt
        )

    private suspend fun Env.isFavorite(url: URL): Boolean =
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.count(Filters.eq("url", url.toExternalForm())) > 0
            }
        }

    private inline val Application.countryCode: String
        get() = resources.configuration.locale.country

}
