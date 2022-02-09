package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsModule
import com.oliynick.max.reader.app.feature.article.list.ArticlesModule
import com.oliynick.max.reader.app.feature.article.list.IosShareArticle
import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual val IO = Dispatchers.Default

fun Environment(
    scope: CoroutineScope,
): Environment =
    object : Environment,
        AppModule<Environment> by AppModule(),
        ArticlesModule<Environment> by ArticlesModule(IosShareArticle),
        ArticleDetailsModule by ArticleDetailsModule(),
        LocalStorage by LocalStorage(),
        NewsApi by NewsApi(),
        CoroutineScope by scope {
    }

actual interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule,
    LocalStorage,
    NewsApi,
    CoroutineScope