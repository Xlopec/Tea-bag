package com.oliynick.max.reader.article.list

import android.app.Application
import com.google.gson.Gson
import com.oliynick.max.reader.app.LocalStorage

fun <Env> ArticlesModule(
    gson: Gson,
    application: Application,
): ArticlesModule<Env> where Env : NewsApi,
                             Env : LocalStorage =

    object : ArticlesModule<Env>,
        ArticlesUpdater by LiveArticlesUpdater,
        ArticlesResolver<Env> by ArticlesResolver(gson, application) {
    }