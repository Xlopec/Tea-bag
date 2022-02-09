@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsUpdater
import com.oliynick.max.reader.app.feature.article.list.ArticlesResolver
import com.oliynick.max.reader.app.feature.article.list.ArticlesUpdater
import com.oliynick.max.reader.app.navigation.AppNavigation
import com.oliynick.max.reader.app.storage.LocalStorage

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