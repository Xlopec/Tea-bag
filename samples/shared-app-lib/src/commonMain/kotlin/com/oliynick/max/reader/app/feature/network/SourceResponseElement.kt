/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.reader.app.domain.SourceDescription
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.domain.SourceName
import io.github.xlopec.tea.data.Url
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
