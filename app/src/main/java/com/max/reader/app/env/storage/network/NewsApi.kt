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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.annotations.SerializedName
import com.max.reader.app.env.storage.TypeAdapter
import com.max.reader.domain.Author
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.domain.tryCreate
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.lang.reflect.Type
import java.net.URL
import java.util.*

val articleAdapters = mapOf(
    String::class to StringAdapter,
    URL::class to URLAdapter,
    Title::class to TitleAdapter,
    Author::class to AuthorAdapter,
    Description::class to DescriptionAdapter
)

fun NewsApi(retrofit: Retrofit): HasNewsApi = object : HasNewsApi {
    override val api = retrofit.create<RetrofitNewsApi>()
}

interface HasNewsApi {
    val api: RetrofitNewsApi
}

interface RetrofitNewsApi {

    @GET("/v2/everything")
    suspend fun fetchFromEverything(
        @Query("apiKey") apiKey: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @QueryMap query: Map<String, String>
    ): ArticleResponse

    @GET("/v2/top-headlines")
    suspend fun fetchTopHeadlines(
        @Query("apiKey") apiKey: String,
        @Query("country") countryCode: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @QueryMap query: Map<String, String>
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
    val urlToImage: URL?
)

data class ArticleResponse(
    @SerializedName("totalResults")
    val totalResults: Int,
    @SerializedName("articles")
    val articles: List<ArticleElement>
)

private object StringAdapter : TypeAdapter<String> {
    override fun serialize(
        src: String,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ) =
        JsonPrimitive(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ) = json.asString.takeUnless { string -> string.isBlank() || string.isEmpty() }
}

private object URLAdapter : TypeAdapter<URL> {
    override fun serialize(
        src: URL,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ) =
        JsonPrimitive(src.toExternalForm())

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ) = URL(json.asString)
}

private object TitleAdapter : TypeAdapter<Title> {
    override fun serialize(
        src: Title,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ) = Title.tryCreate(json.asString)
}

private object AuthorAdapter : TypeAdapter<Author> {
    override fun serialize(
        src: Author,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ) = Author.tryCreate(json.asString)
}

private object DescriptionAdapter : TypeAdapter<Description> {
    override fun serialize(
        src: Description,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ) = Description.tryCreate(json.asString)
}
