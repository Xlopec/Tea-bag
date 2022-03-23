@file:Suppress("TestFunctionName")

package com.max.reader.environment

import com.oliynick.max.entities.shared.datatypes.Left
import com.oliynick.max.reader.app.feature.network.ArticleElement
import com.oliynick.max.reader.app.feature.network.ArticleResponse
import kotlinx.coroutines.delay

fun foreverWaitingResponse(): ArticleResponseProvider = { _, _ ->
    delay(Long.MAX_VALUE)
    error("Should never get here")
}

fun anyArticleRequest(): ArticlePredicate = { _, _ -> true }

fun ArticleResponse(
    vararg articles: ArticleElement
): Left<ArticleResponse> =
    Left(
        ArticleResponse(
            articles.size,
            articles.toList()
        )
    )