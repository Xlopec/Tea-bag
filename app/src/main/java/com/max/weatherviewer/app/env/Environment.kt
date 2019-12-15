package com.max.weatherviewer.app.env

import android.app.Application
import com.google.gson.Gson
import com.max.weatherviewer.app.env.network.HasNewsApi
import com.max.weatherviewer.app.env.network.NewsApi
import com.max.weatherviewer.app.env.storage.*
import com.max.weatherviewer.app.resolve.AppResolver
import com.max.weatherviewer.app.resolve.CommandTransport
import com.max.weatherviewer.app.resolve.HasCommandTransport
import com.max.weatherviewer.app.update.AppUpdater
import com.max.weatherviewer.screens.feed.resolve.FeedResolver
import com.max.weatherviewer.screens.feed.resolve.LiveFeedResolver
import com.max.weatherviewer.screens.feed.update.FeedUpdater
import com.max.weatherviewer.screens.feed.update.LiveFeedUpdater
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit

interface Environment :
    AppUpdater<Environment>,
    AppResolver<Environment>,
    FeedUpdater,
    HasCommandTransport,
    HasAppContext,
    FeedResolver<Environment>,
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
    application: Application,
    gson: Gson,
    isDebug: Boolean
): Environment = object : Environment,
    AppUpdater<Environment> by AppUpdater(),
    FeedUpdater by LiveFeedUpdater,
    AppResolver<Environment> by AppResolver(),
    HasCommandTransport by CommandTransport(),
    FeedResolver<Environment> by LiveFeedResolver(),
    HasNewsApi by NewsApi(retrofit),
    HasMongoCollection by MongoCollection(
        application
    ),
    HasGson by Gson(
        gson
    ),
    HasAppContext by AppContext(application),
    Storage<Environment> by Storage(),
    CoroutineScope by scope {
    override val isDebug: Boolean = isDebug
}
