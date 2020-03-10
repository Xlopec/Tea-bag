@file:Suppress("FunctionName")

package com.max.weatherviewer.app.env.storage.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.annotations.SerializedName
import com.max.weatherviewer.app.env.storage.TypeAdapter
import com.max.weatherviewer.domain.Author
import com.max.weatherviewer.domain.Description
import com.max.weatherviewer.domain.Title
import com.max.weatherviewer.domain.tryCreate
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
        @QueryMap query: Map<String, String>
    ): ArticleResponse

    @GET("/v2/top-headlines")
    suspend fun fetchTopHeadlines(
        @Query("apiKey") apiKey: String,
        @Query("country") countryCode: String
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
    @SerializedName("articles")
    val articles: List<ArticleElement> = listOf()
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