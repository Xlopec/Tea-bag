@file:Suppress("TestFunctionName")

package com.max.reader.environment

import com.oliynick.max.entities.shared.datatypes.Left
import com.oliynick.max.reader.network.ArticleElement
import kotlinx.coroutines.delay

fun foreverWaitingResponse(): ResponseProvider = { _, _ ->
    delay(Long.MAX_VALUE)
    error("Should never get here")
}

fun anyRequest(): InputPredicate = { _, _ -> true }

fun ArticleResponse(
    vararg articles: ArticleElement
) =
    Left(
        com.oliynick.max.reader.network.ArticleResponse(
            articles.size,
            articles.toList()
        )
    )