package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.reader.app.domain.SourceDescription
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.domain.SourceName
import com.oliynick.max.tea.data.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceResponseElement(
    @SerialName("id")
    @Serializable(with = SourceIdSerializer::class)
    val id: SourceId,
    @SerialName("name")
    @Serializable(with = SourceNameSerializer::class)
    val name: SourceName,
    @SerialName("description")
    @Serializable(with = SourceDescriptionSerializer::class)
    val description: SourceDescription?,
    @SerialName("url")
    @Serializable(with = UrlSerializer::class)
    val url: Url
)

@Serializable
data class SourcesResponse(
    @SerialName("sources")
    val sources: List<SourceResponseElement>
)
