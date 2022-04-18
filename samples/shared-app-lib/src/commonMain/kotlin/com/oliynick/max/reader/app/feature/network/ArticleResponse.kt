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

import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.domain.Title
import io.github.xlopec.tea.data.Date
import io.github.xlopec.tea.data.Url
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
    @SerialName("source")
    val source: SourceElement,
)

@Serializable
data class SourceElement(
    @SerialName("id")
    @Serializable(with = SourceIdSerializer::class)
    val id: SourceId?,
)

@Serializable
data class ArticleResponse(
    @SerialName("totalResults")
    val totalResults: Int,
    @SerialName("articles")
    val articles: List<ArticleElement>
)
