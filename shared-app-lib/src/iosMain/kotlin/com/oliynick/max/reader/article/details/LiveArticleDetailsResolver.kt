@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.details

import com.oliynick.max.reader.app.PlatformEnv

actual interface ArticleDetailsEnv

actual fun <Env : ArticleDetailsEnv> ArticleDetailsResolver(): ArticleDetailsResolver<Env> =
    ArticleDetailsResolver { message ->
        setOf()
    }

actual fun ArticleDetailsEnv(platformEnv: PlatformEnv): ArticleDetailsEnv = object : ArticleDetailsEnv {

}