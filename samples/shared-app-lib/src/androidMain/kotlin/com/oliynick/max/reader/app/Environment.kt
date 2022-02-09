@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import android.app.Application
import android.os.StrictMode.*
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsModule
import com.oliynick.max.reader.app.feature.article.list.AndroidShareArticle
import com.oliynick.max.reader.app.feature.article.list.ArticlesModule
import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.navigation.AppNavigation
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual val IO: CoroutineDispatcher = Dispatchers.IO

fun Environment(
    debug: Boolean,
    application: Application,
    scope: CoroutineScope,
    closeCommands: CloseCommandsSink
): Environment {

    if (debug) {
        setupStrictAppPolicies()
    }

    return object : Environment,
        AppModule<Environment> by AppModule(closeCommands),
        ArticlesModule<Environment> by ArticlesModule(AndroidShareArticle(application)),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(application),
        NewsApi by NewsApi(application),
        LocalStorage by LocalStorage(application),
        CoroutineScope by scope {
        }
}

private fun setupStrictAppPolicies() {
    setThreadPolicy(
        ThreadPolicy.Builder()
            .detectAll()
            .penaltyFlashScreen()
            .penaltyLog()
            .build()
    )

    setVmPolicy(
        VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}

actual interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule<Environment>,
    NewsApi,
    LocalStorage,
    AppNavigation,
    CoroutineScope