@file:Suppress("FunctionName")

package com.max.weatherviewer.app.env.storage

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.max.weatherviewer.app.env.HasAppContext
import com.max.weatherviewer.app.env.network.ArticleElement
import com.max.weatherviewer.app.env.network.ArticleResponse
import com.max.weatherviewer.app.env.network.HasNewsApi
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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
        toArticles(api.fetchFromEverything(API_KEY, mapOf("q" to criteria.query)))

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

interface HasMongoCollection {
    val collection: MongoCollection<Document>
}

fun MongoCollection(
    context: Context
): HasMongoCollection = object :
    HasMongoCollection {
    override val collection: MongoCollection<Document>

    init {
        Stitch.initialize(context)

        val client = Stitch.initializeAppClient(context.packageName)

        collection = client.getServiceClient(LocalMongoDbService.clientFactory)
            .getDatabase("app")
            .getCollection("favorite")
    }
}

interface HasGson {
    val gson: Gson
}

fun Gson(gson: Gson) = object : HasGson {
    override val gson: Gson = gson
}
