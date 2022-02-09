@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.details

fun ArticleDetailsResolver(): ArticleDetailsResolver =
    ArticleDetailsResolver { message ->
        setOf()
    }