@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.details

fun ArticleDetailsResolver(): ArticleDetailsResolver =
    ArticleDetailsResolver { message ->
        setOf()
    }