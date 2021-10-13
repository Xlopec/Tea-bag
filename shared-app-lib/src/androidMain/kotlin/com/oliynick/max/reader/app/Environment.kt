@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import android.app.Application
import android.os.StrictMode
import com.oliynick.max.reader.app.serialization.ArticleAdapters
import com.oliynick.max.reader.article.details.ArticleDetailsModule
import com.oliynick.max.reader.article.list.ArticlesModule
import com.oliynick.max.reader.article.list.NewsApi
import kotlinx.coroutines.CoroutineScope

actual interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule<Environment>,
    NewsApi,
    LocalStorage,
    AppNavigation,
    CoroutineScope

fun Environment(
    debug: Boolean,
    application: Application,
    scope: CoroutineScope,
    closeCommands: CloseCommandsSink
): Environment {

    val gson = BuildGson()

    if (debug) {
        setupStrictAppPolicies()
    }

    return object : Environment,
        AppModule<Environment> by AppModule(closeCommands),
        ArticlesModule<Environment> by ArticlesModule(gson, application),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(application),
        NewsApi by NewsApi(application),
        LocalStorage by LocalStorage(application),
        CoroutineScope by scope {
        }
}

private fun setupStrictAppPolicies() {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyFlashScreen()
            .penaltyLog()
            .build()
    )

    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}

private fun BuildGson() =
    AppGson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        ArticleAdapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }
