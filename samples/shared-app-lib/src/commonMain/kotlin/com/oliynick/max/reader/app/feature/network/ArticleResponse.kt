package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.entities.shared.Date
import com.oliynick.max.entities.shared.Url
import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.Title
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleElement(
    @Serializable(with = AuthorSerializer::class)
    @SerialName("author")
    val author: Author? = null,
    @SerialName("description")
    @Serializable(with = DescriptionSerializer::class)
    val description: Description? = null,
    @SerialName("publishedAt")
    @Serializable(with = CommonDateSerializer::class)
    val publishedAt: Date,
    @SerialName("title")
    @Serializable(with = TitleSerializer::class)
    val title: Title,
    @SerialName("url")
    @Serializable(with = UrlSerializer::class)
    val url: Url,
    @SerialName("urlToImage")
    @Serializable(with = UrlSerializer::class)
    val urlToImage: Url? = null,
)

@Serializable
data class ArticleResponse(
    @SerialName("totalResults")
    val totalResults: Int,
    @SerialName("articles")
    val articles: List<ArticleElement>
)