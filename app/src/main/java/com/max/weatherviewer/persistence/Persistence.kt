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
import org.bson.Document

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

    suspend fun Env.getFavorite(): List<Article>

}

interface MongoDbStorage<Env> : Storage<Env> where Env : HasMongoCollection,
                                                   Env : HasGson {

    override suspend fun Env.addToFavorite(article: Article) {
        val r = collection.replaceOne(
            Filters.eq("url", article.url.toExternalForm()),
            Document.parse(gson.toJson(article)),
            ReplaceOptions.createReplaceOptions(UpdateOptions().upsert(true))
        )

        println(r)
    }

    override suspend fun Env.getFavorite(): List<Article> {
        return collection
            .find()
            .map { gson.fromJson(it.toJson(), Article::class.java) }
            .into(ArrayList())
    }
}

/*
suspend inline fun <reified T> Context.load(gson: Gson, crossinline ifNone: () -> T): T {
    return withContext(Dispatchers.IO) {
        val file = cacheFile(T::class.java.fileName)

        val state = runCatching { gson.fromJson(FileReader(file), T::class.java) }
            .onFailure { file.delete() }
            .getOrElse { ifNone() }
            ?: ifNone()

        state
    }
}

suspend inline fun <reified T> Context.persist(gson: Gson, state: T) {
    return withContext(Dispatchers.IO) { cacheFile(T::class.java.fileName).writeText(gson.toJson(state)) }
}

val Class<*>.fileName get() = "$simpleName.json"
fun Context.cacheFile(filename: String): File = File(cacheDir, filename).also { it.createNewFile() }*/
