package com.oliynick.max.reader.network

import com.google.gson.annotations.SerializedName
import com.oliynick.max.reader.domain.*

internal actual data class ArticleElement(
    @SerializedName("author")
    actual val author: Author?,
    @SerializedName("description")
    actual val description: Description?,
    @SerializedName("publishedAt")
    actual val publishedAt: CommonDate,
    @SerializedName("title")
    actual val title: Title,
    @SerializedName("url")
    actual val url: Url,
    @SerializedName("urlToImage")
    actual val urlToImage: Url?,
)

internal actual data class ArticleResponse(
    @SerializedName("totalResults")
    actual val totalResults: Int,
    @SerializedName("articles")
    actual val articles: List<ArticleElement>,
)