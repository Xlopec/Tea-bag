package com.oliynick.max.reader.app

import com.oliynick.max.reader.article.details.ArticleDetailsModule
import com.oliynick.max.reader.article.list.ArticlesModule
import com.oliynick.max.reader.article.list.NewsApi
import kotlinx.coroutines.CoroutineScope

actual interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule<Environment>,
    LocalStorage,
    NewsApi<Environment>,
    CoroutineScope

fun Environment(
    scope: CoroutineScope,
    closeCommandsSink: CloseCommandsSink
): Environment {

    return object : Environment,
        AppModule<Environment> by AppModule(closeCommandsSink),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        LocalStorage by LocalStorage(),
        NewsApi<Environment> by NewsApi(),
        CoroutineScope by scope {
    }
}