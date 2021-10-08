package com.oliynick.max.reader.app

import com.oliynick.max.reader.article.details.ArticleDetailsEnv
import com.oliynick.max.reader.article.details.ArticleDetailsModule
import com.oliynick.max.reader.article.list.ArticlesEnv
import com.oliynick.max.reader.article.list.ArticlesModule
import com.oliynick.max.reader.article.list.NewsApi
import com.oliynick.max.reader.article.list.NewsApiEnv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow

interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule<Environment>,
    ArticleDetailsEnv,
    ArticlesEnv,
    NewsApiEnv,
    NewsApi<Environment>,
    LocalStorage,
    AppNavigation,
    CoroutineScope

expect interface PlatformEnv {
    val closeCommands: CloseCommandsSink
}

fun Environment(
    platform: PlatformEnv
): Environment {

    val scope = CoroutineScope(Job() + Dispatchers.Default)

    return object : Environment,
        AppModule<Environment> by AppModule(platform),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        NewsApi<Environment> by NewsApi(),
        NewsApiEnv by NewsApiEnv(platform),
        LocalStorage by LocalStorage(platform),
        ArticleDetailsEnv by ArticleDetailsEnv(platform),
        ArticlesEnv by ArticlesEnv(platform),
        CoroutineScope by scope {

    }
}
