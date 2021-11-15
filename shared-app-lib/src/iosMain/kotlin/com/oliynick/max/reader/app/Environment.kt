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
    NewsApi,
    CoroutineScope

fun Environment(
    scope: CoroutineScope,
): Environment =
    object : Environment,
        AppModule<Environment> by AppModule(),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        LocalStorage by LocalStorage(),
        NewsApi by NewsApi(),
        CoroutineScope by scope {
    }