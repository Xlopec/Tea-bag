package com.oliynick.max.reader.app

import com.oliynick.max.reader.article.details.ArticleDetailsEnv
import com.oliynick.max.reader.article.details.ArticleDetailsModule
import com.oliynick.max.reader.article.list.ArticlesEnv
import com.oliynick.max.reader.article.list.ArticlesModule
import com.oliynick.max.reader.article.list.NewsApi
import com.oliynick.max.reader.article.list.NewsApiEnv
import kotlinx.coroutines.CoroutineScope

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

expect interface PlatformEnv

expect fun Environment(
    platform: PlatformEnv
): Environment
