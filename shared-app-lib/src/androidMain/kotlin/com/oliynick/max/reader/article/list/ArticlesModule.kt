@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.list

import android.app.Application
import com.oliynick.max.reader.app.LocalStorage

fun <Env> ArticlesModule(
    application: Application,
): ArticlesModule<Env> where Env : NewsApi,
                             Env : LocalStorage =

    object : ArticlesModule<Env>,
        ArticlesUpdater by LiveArticlesUpdater,
        ArticlesResolver<Env> by ArticlesResolver(application) {
    }