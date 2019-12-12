package com.max.weatherviewer.persistence

import android.content.Context
import com.google.gson.Gson
import com.max.weatherviewer.domain.Article
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
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

interface HasMongoDb {
    val mongoDb: MongoDatabase
}

interface HasMongoCollection {
    val collection: MongoCollection<Document>
}

fun HasMongoCollection(
    context: Context
): HasMongoCollection = object : HasMongoCollection {
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

fun HasGson(gson: Gson) = object : HasGson {
    override val gson: Gson = gson
}

fun <Env> Storage(): Storage<Env> where Env : HasMongoCollection,
                                        Env : HasGson = object : MongoDbStorage<Env> {

}

interface Storage<Env> {

    suspend fun Env.addToFavorite(article: Article)

    suspend fun Env.removeFromFavorite(url: URL)

    suspend fun Env.getFavorite(): List<Article>

    @Deprecated("temp workaround")
    suspend fun Env.isFavorite(url: URL): Boolean

}

interface MongoDbStorage<Env> : Storage<Env> where Env : HasMongoCollection,
                                                   Env : HasGson {

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

    override suspend fun Env.getFavorite(): List<Article> =
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection
                    .find()
                    .map { gson.fromJson(it.toJson(), Article::class.java) }
                    .into(ArrayList())
            }
        }

    override suspend fun Env.isFavorite(url: URL): Boolean =
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.count(Filters.eq("url", url.toExternalForm())) > 0
            }
        }

}
