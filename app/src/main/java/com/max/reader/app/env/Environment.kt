package com.max.reader.app.env

import android.app.Application
import com.max.reader.app.env.storage.*
import com.max.reader.app.env.storage.local.HasMongoCollection
import com.max.reader.app.env.storage.local.MongoCollection
import com.max.reader.app.env.storage.network.*
import com.max.reader.app.resolve.*
import com.max.reader.app.update.AppUpdater
import com.max.reader.screens.feed.resolve.FeedResolver
import com.max.reader.screens.feed.resolve.LiveFeedResolver
import com.max.reader.screens.feed.update.FeedUpdater
import com.max.reader.screens.feed.update.LiveFeedUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.*
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
): Environment {

    val gson = buildGson()
    val retrofit = buildRetrofit(gson)

    return object : Environment,
                    AppUpdater<Environment> by AppUpdater(),
                    FeedUpdater by LiveFeedUpdater,
                    AppResolver<Environment> by AppResolver(),
                    HasCommandTransport by CommandTransport(),
                    FeedResolver<Environment> by LiveFeedResolver(),
                    HasNewsApi by NewsApi(retrofit),
                    HasMongoCollection by MongoCollection(application),
                    HasGson by Gson(gson),
                    HasAppContext by AppContext(application),
                    Storage<Environment> by Storage(),
                    CoroutineScope by AppComponentScope {
        override val isDebug: Boolean = isDebug
    }
}

private fun buildGson() =
    gson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        articleAdapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }

private object AppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}