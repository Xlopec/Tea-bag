package com.max.reader.app.env

import android.app.Application
import com.max.reader.app.AppModule
import com.max.reader.app.env.storage.Gson
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.Storage
import com.max.reader.app.env.storage.local.HasMongoCollection
import com.max.reader.app.env.storage.local.MongoCollection
import com.max.reader.app.env.storage.network.HasNewsApi
import com.max.reader.app.env.storage.network.NewsApi
import com.max.reader.app.env.storage.network.articleAdapters
import com.max.reader.app.resolve.CommandTransport
import com.max.reader.app.resolve.HasCommandTransport
import com.max.reader.screens.article.details.ArticleDetailsModule
import com.max.reader.screens.article.list.ArticlesModule
import kotlinx.coroutines.CoroutineScope

interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule<Environment>,
    HasCommandTransport,
    HasAppContext,
    HasNewsApi,
    HasMongoCollection,
    HasGson,
    Storage<Environment>,
    CoroutineScope

@Suppress("FunctionName")
fun Environment(
    application: Application,
    scope: CoroutineScope,
): Environment {

    val gson = buildGson()
    val retrofit = Retrofit(gson)

    return object : Environment,
        AppModule<Environment> by AppModule(),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        HasCommandTransport by CommandTransport(),
        HasNewsApi by NewsApi(retrofit),
        HasMongoCollection by MongoCollection(application),
        HasGson by Gson(gson),
        HasAppContext by AppContext(application),
        Storage<Environment> by Storage(),
        CoroutineScope by scope {
    }
}

private fun buildGson() =
    AppGson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        articleAdapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }