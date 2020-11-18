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
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.details.resolve.LiveArticleDetailsResolver
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.details.update.LiveArticleDetailsUpdater
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.max.reader.screens.article.list.resolve.LiveArticlesResolver
import com.max.reader.screens.article.list.update.ArticlesUpdater
import com.max.reader.screens.article.list.update.LiveArticlesUpdater
import kotlinx.coroutines.CoroutineScope

interface Environment :
    AppUpdater<Environment>,
    AppResolver<Environment>,
    ArticlesUpdater,
    ArticleDetailsUpdater,
    ArticleDetailsResolver<Environment>,
    HasCommandTransport,
    HasAppContext,
    ArticlesResolver<Environment>,
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
    scope: CoroutineScope,
): Environment {

    val gson = buildGson()
    val retrofit = Retrofit(gson)

    return object : Environment,
        AppUpdater<Environment> by AppUpdater(),
        ArticlesUpdater by LiveArticlesUpdater,
        ArticleDetailsUpdater by LiveArticleDetailsUpdater,
        ArticleDetailsResolver<Environment> by LiveArticleDetailsResolver(),
        AppResolver<Environment> by AppResolver(),
        HasCommandTransport by CommandTransport(),
        ArticlesResolver<Environment> by LiveArticlesResolver(),
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