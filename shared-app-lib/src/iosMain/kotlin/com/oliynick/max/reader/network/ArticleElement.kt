package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.*

internal actual class ArticleElement {
    actual val author: Author?
        get() = TODO("Not yet implemented")
    actual val description: Description?
        get() = TODO("Not yet implemented")
    actual val publishedAt: CommonDate
        get() = TODO("Not yet implemented")
    actual val title: Title
        get() = TODO("Not yet implemented")
    actual val url: Url
        get() = TODO("Not yet implemented")
    actual val urlToImage: Url?
        get() = TODO("Not yet implemented")
}

internal actual class ArticleResponse {
    actual val totalResults: Int
        get() = TODO("Not yet implemented")
    actual val articles: List<ArticleElement>
        get() = TODO("Not yet implemented")
}