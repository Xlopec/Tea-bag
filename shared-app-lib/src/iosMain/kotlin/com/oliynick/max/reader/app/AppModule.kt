@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.article.details.ArticleDetailsUpdater
import com.oliynick.max.reader.article.list.ArticlesResolver
import com.oliynick.max.reader.article.list.ArticlesUpdater

fun <Env> AppModule(
    closeCommands: CloseCommandsSink,
): AppModule<Env> where Env : ArticlesResolver<Env>,
                        Env : ArticleDetailsResolver,
                        Env : ArticlesUpdater,
                        Env : LocalStorage,
                        Env : AppNavigation,
                        Env : ArticleDetailsUpdater =
    object : AppModule<Env>,
        AppNavigation by AppNavigation(),
        AppUpdater<Env> by AppUpdater(),
        AppResolver<Env> by AppResolverImpl(closeCommands) {

    }