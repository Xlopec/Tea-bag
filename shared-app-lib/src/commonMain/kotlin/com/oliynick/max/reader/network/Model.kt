package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.*

/*
 fixme: actually there should be only one model for both targets but since I don't want to break
 remote debugger and I don't have multiplatform adapter yet, let's create actual implementations:
 one that uses Gson (jvm only) and another one that uses multiplatform kotlinx serialization plugin
 (multiplatform one)
 */

internal expect class ArticleElement {
    val author: Author?
    val description: Description?
    val publishedAt: CommonDate
    val title: Title
    val url: Url
    val urlToImage: Url?
}

internal expect class ArticleResponse {
    val totalResults: Int
    val articles: List<ArticleElement>
}

// fixme temp
internal operator fun ArticleResponse.component1() = totalResults

internal operator fun ArticleResponse.component2() = articles
