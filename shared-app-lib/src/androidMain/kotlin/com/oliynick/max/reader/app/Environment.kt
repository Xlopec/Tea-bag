@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import android.app.Application
import android.os.StrictMode
import com.google.gson.Gson
import com.oliynick.max.reader.app.serialization.ArticleAdapters
import com.oliynick.max.reader.article.details.ArticleDetailsEnv
import com.oliynick.max.reader.article.details.ArticleDetailsModule
import com.oliynick.max.reader.article.list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

actual interface PlatformEnv {
    val debug: Boolean
    val application: Application
    val gson: Gson
    actual val closeCommands: CloseCommandsSink
}

/*fun PlatformEnv(
    debug: Boolean,
    application: Application,
    closeCommands: MutableSharedFlow<CloseApp>
): PlatformEnv = object : PlatformEnv {
    override val debug: Boolean = debug
    override val application: Application = application
    override val gson: Gson = BuildGson()
}*/

/*actual fun Environment(
    platform: PlatformEnv
): Environment {

    val gson = BuildGson()

    if (platform.debug) {
        setupStrictAppPolicies()
    }

    return object : Environment,
        AppModule<Environment> by AppModule(platform),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        NewsApi<Environment> by NewsApi(),
        NewsApiEnv by NewsApiEnv(platform.application),
        LocalStorage by LocalStorage(platform.application),
        ArticleDetailsEnv by ArticleDetailsEnv(platform),
        ArticlesEnv by ArticlesEnv(platform),
        CoroutineScope by platform.scope {

        override val application: Application = platform.application
    }
}*/

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
