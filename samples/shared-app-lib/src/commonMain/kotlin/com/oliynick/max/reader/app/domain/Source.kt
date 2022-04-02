package com.oliynick.max.reader.app.domain

import com.oliynick.max.tea.data.Url
import kotlin.jvm.JvmInline

data class Source(
    val id: SourceId,
    val name: SourceName,
    val description: SourceDescription?,
    val url: Url,
    val logo: Url,
)

@JvmInline
value class SourceName(
    val value: String,
)

@JvmInline
value class SourceDescription(
    val value: String,
)

@JvmInline
value class SourceId(
    val value: String,
)
