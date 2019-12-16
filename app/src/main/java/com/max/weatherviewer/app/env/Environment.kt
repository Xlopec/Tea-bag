package com.max.weatherviewer.app.env

import android.app.Application
import com.max.weatherviewer.app.env.storage.Gson
import com.max.weatherviewer.app.env.storage.HasGson
import com.max.weatherviewer.app.env.storage.Storage
import com.max.weatherviewer.app.env.storage.local.HasMongoCollection
import com.max.weatherviewer.app.env.storage.local.MongoCollection
import com.max.weatherviewer.app.env.storage.network.HasNewsApi
import com.max.weatherviewer.app.env.storage.network.NewsApi
import com.max.weatherviewer.app.env.storage.network.articleAdapters
import com.max.weatherviewer.app.resolve.AppResolver
import com.max.weatherviewer.app.resolve.CommandTransport
import com.max.weatherviewer.app.resolve.HasCommandTransport
import com.max.weatherviewer.app.update.AppUpdater
import com.max.weatherviewer.misc.unsafeLazy
import com.max.weatherviewer.screens.feed.resolve.FeedResolver
import com.max.weatherviewer.screens.feed.resolve.LiveFeedResolver
import com.max.weatherviewer.screens.feed.update.FeedUpdater
import com.max.weatherviewer.screens.feed.update.LiveFeedUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

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
    application: Application,
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
    CoroutineScope by AppComponentScope {
    override val isDebug: Boolean = isDebug
}


private val gson by unsafeLazy {
    gson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        articleAdapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }
}

private val retrofit by unsafeLazy {
    retrofit(gson)
}

private object AppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}