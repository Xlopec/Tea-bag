package com.max.reader.app.env

import android.app.Application
import com.max.reader.app.env.storage.Gson
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.Storage
import com.max.reader.app.env.storage.local.HasMongoCollection
import com.max.reader.app.env.storage.local.MongoCollection
import com.max.reader.app.env.storage.network.HasNewsApi
import com.max.reader.app.env.storage.network.NewsApi
import com.max.reader.app.env.storage.network.articleAdapters
import com.max.reader.app.resolve.AppResolver
import com.max.reader.app.resolve.CommandTransport
import com.max.reader.app.resolve.HasCommandTransport
import com.max.reader.app.update.AppUpdater
import com.max.reader.screens.feed.resolve.FeedResolver
import com.max.reader.screens.feed.resolve.LiveFeedResolver
import com.max.reader.screens.feed.update.FeedUpdater
import com.max.reader.screens.feed.update.LiveFeedUpdater
import kotlinx.coroutines.CoroutineScope

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
    isDebug: Boolean,
    scope: CoroutineScope
): Environment {

    val gson = buildGson()
    val retrofit = Retrofit(gson)

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
                    CoroutineScope by scope {
        override val isDebug: Boolean = isDebug
    }
}

private fun buildGson() =
    AppGson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        articleAdapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }