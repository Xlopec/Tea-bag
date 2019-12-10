package com.max.weatherviewer.env

import android.content.Context
import com.google.gson.Gson
import com.max.weatherviewer.app.AppResolver
import com.max.weatherviewer.app.AppUpdater
import com.max.weatherviewer.app.CommandTransport
import com.max.weatherviewer.app.HasCommandTransport
import com.max.weatherviewer.home.*
import com.max.weatherviewer.persistence.HasGson
import com.max.weatherviewer.persistence.HasMongoCollection
import com.max.weatherviewer.persistence.Storage
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService
import kotlinx.coroutines.CoroutineScope
import org.bson.Document
import retrofit2.Retrofit

interface Environment :
    AppUpdater<Environment>,
    AppResolver<Environment>,
    FeedUpdater,
    HasCommandTransport,
    HomeResolver<Environment>,
    HasNewsApi,
    HasMongoCollection,
    HasGson,
    Storage<Environment>,
    CoroutineScope {
    val isDebug: Boolean
}

@Suppress("FunctionName")
fun Environment(
    retrofit: Retrofit,
    scope: CoroutineScope,
    ctx: Context,
    gson: Gson,
    isDebug: Boolean
): Environment = object : Environment,
    AppUpdater<Environment> by AppUpdater(),
    FeedUpdater by LiveFeedUpdater,
    AppResolver<Environment> by AppResolver(),
    HasCommandTransport by CommandTransport(),
    HomeResolver<Environment> by HomeResolver(),
    HasNewsApi by NewsApi(retrofit),
    HasMongoCollection by HasMongoCollection(ctx),
    HasGson by HasGson(gson),
    Storage<Environment> by Storage(),
    CoroutineScope by scope {
    override val isDebug: Boolean = isDebug
}

fun s(c: Context) {
    Stitch.initialize(c)
    val client = Stitch.initializeAppClient(c.packageName)

    client.getServiceClient(LocalMongoDbService.clientFactory)
        .getDatabase("app").getCollection("favorite").insertOne(
            Document.parse("")
        )
}