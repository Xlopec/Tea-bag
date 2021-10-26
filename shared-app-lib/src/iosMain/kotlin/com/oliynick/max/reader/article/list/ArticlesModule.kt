@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.LocalStorage

fun <Env> ArticlesModule(): ArticlesModule<Env> where Env : NewsApi, Env : LocalStorage =

    object : ArticlesModule<Env>,
        ArticlesUpdater by LiveArticlesUpdater,
        ArticlesResolver<Env> by ArticlesResolver() {
    }