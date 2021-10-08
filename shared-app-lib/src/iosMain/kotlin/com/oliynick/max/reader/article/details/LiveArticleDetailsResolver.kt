@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.details

actual interface ArticleDetailsEnv

actual fun <Env : ArticleDetailsEnv> ArticleDetailsResolver(): ArticleDetailsResolver<Env> =
    ArticleDetailsResolver { message ->
        setOf()
    }