@file:Suppress("FunctionName")

package com.max.reader.screens.article.list

import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.Storage
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.max.reader.screens.article.list.resolve.LiveArticlesResolver
import com.max.reader.screens.article.list.update.ArticlesUpdater
import com.max.reader.screens.article.list.update.LiveArticlesUpdater

interface ArticlesModule<Env> : ArticlesUpdater, ArticlesResolver<Env>

fun <Env> ArticlesModule(): ArticlesModule<Env> where Env : HasAppContext,
                                                      Env : HasGson,
                                                      Env : Storage<Env> =

    object : ArticlesModule<Env>,
        ArticlesUpdater by LiveArticlesUpdater,
        ArticlesResolver<Env> by LiveArticlesResolver() {
    }
