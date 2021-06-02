/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package com.max.reader.app.env.storage.network

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.max.reader.app.env.storage.TypeAdapter
import com.max.reader.domain.Author
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.domain.tryCreate
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.JsonSerializer
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import java.lang.reflect.Type
import java.net.URL
import java.util.*

private const val ApiKey = "08a7e13902bf4cffab115365071e3850"

val ArticleAdapters = mapOf(
    String::class to StringAdapter,
    URL::class to URLAdapter,
    Title::class to TitleAdapter,
    Author::class to AuthorAdapter,
    Description::class to DescriptionAdapter
)

fun NewsApi(
    gson: Gson,
    debug: Boolean,
): NewsApi = object : NewsApi {

    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer(gson)
        }

        if (debug) {
            Logging {
                level = LogLevel.ALL
            }
        }
    }

    override suspend fun fetchFromEverything(
        page: Int,
        pageSize: Int,
        query: Map<String, String>,
    ): ArticleResponse =
        httpClient.get(EverythingRequest(page, pageSize, query))

    override suspend fun fetchTopHeadlines(
        countryCode: String,
        page: Int,
        pageSize: Int,
        query: Map<String, String>,
    ): ArticleResponse =
        httpClient.get(TopHeadlinesRequest(page, pageSize, countryCode, query))
}

private class GsonSerializer(
    private val gson: Gson,
) : JsonSerializer {

    override fun write(data: Any, contentType: ContentType): OutgoingContent =
        TextContent(gson.toJson(data), contentType)

    override fun read(type: TypeInfo, body: Input): Any =
        gson.fromJson(body.readText(), type.reifiedType)
}

interface NewsApi {

    suspend fun fetchFromEverything(
        page: Int,
        pageSize: Int,
        query: Map<String, String>,
    ): ArticleResponse

    suspend fun fetchTopHeadlines(
        countryCode: String,
        page: Int,
        pageSize: Int,
        query: Map<String, String>,
    ): ArticleResponse
}

data class ArticleElement(
    @SerializedName("author")
    val author: Author?,
    @SerializedName("description")
    val description: Description?,
    @SerializedName("publishedAt")
    val publishedAt: Date,
    @SerializedName("title")
    val title: Title,
    @SerializedName("url")
    val url: URL,
    @SerializedName("urlToImage")
    val urlToImage: URL?,
)

data class ArticleResponse(
    @SerializedName("totalResults")
    val totalResults: Int,
    @SerializedName("articles")
    val articles: List<ArticleElement>,
)

private object StringAdapter : TypeAdapter<String> {
    override fun serialize(
        src: String,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = json.asString?.takeUnless(CharSequence::isNullOrEmpty)
}

private object URLAdapter : TypeAdapter<URL> {
    override fun serialize(
        src: URL,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.toExternalForm())

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = URL(json.asString)
}

private object TitleAdapter : TypeAdapter<Title> {
    override fun serialize(
        src: Title,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = Title.tryCreate(json.asString)
}

private object AuthorAdapter : TypeAdapter<Author> {
    override fun serialize(
        src: Author,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = Author.tryCreate(json.asString)
}

private object DescriptionAdapter : TypeAdapter<Description> {
    override fun serialize(
        src: Description,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = Description.tryCreate(json.asString)
}

private fun EverythingRequest(
    page: Int,
    pageSize: Int,
    query: Map<String, String>,
) = HttpRequestBuilder(
    scheme = HTTPS.name,
    host = "newsapi.org",
    path = "/v2/everything"
) {
    with(parameters) {
        append("apiKey", ApiKey)
        append("page", page.toString())
        append("pageSize", pageSize.toString())

        query.forEach { (k, v) ->
            append(k, v)
        }
    }
}

private fun TopHeadlinesRequest(
    page: Int,
    pageSize: Int,
    countryCode: String,
    query: Map<String, String>,
) = HttpRequestBuilder(
    scheme = HTTPS.name,
    host = "newsapi.org",
    path = "/v2/top-headlines"
) {
    with(parameters) {
        append("apiKey", ApiKey)
        append("page", page.toString())
        append("pageSize", pageSize.toString())
        append("country", countryCode)

        query.forEach { (k, v) ->
            append(k, v)
        }
    }
}