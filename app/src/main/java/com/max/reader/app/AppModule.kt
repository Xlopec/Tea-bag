@file:Suppress("FunctionName")

package com.max.reader.app

import com.max.reader.app.resolve.AppResolver
import com.max.reader.app.resolve.HasCommandTransport
import com.max.reader.app.update.AppUpdater
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.max.reader.screens.article.list.update.ArticlesUpdater

interface AppModule<Env> : AppUpdater<Env>, AppResolver<Env>

fun <Env> AppModule(): AppModule<Env> where Env : HasCommandTransport,
                                            Env : ArticlesResolver<Env>,
                                            Env : ArticleDetailsResolver<Env>,
                                            Env : ArticlesUpdater,
                                            Env : ArticleDetailsUpdater =
    object : AppModule<Env>,
        AppUpdater<Env> by AppUpdater(),
        AppResolver<Env> by AppResolver() {

    }
