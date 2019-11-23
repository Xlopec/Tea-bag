package com.max.weatherviewer

import com.google.gson.*
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.domain.Author
import com.max.weatherviewer.domain.Description
import com.max.weatherviewer.domain.Title
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type
import java.net.URL

typealias NewsApi = suspend (String) -> List<Article>

interface TypeAdapter<T> : JsonSerializer<T>, JsonDeserializer<T>

val adapters = mapOf(
    URL::class to URLAdapter,
    Title::class to TitleAdapter,
    Author::class to AuthorAdapter,
    Description::class to DescriptionAdapter
)

fun newsApi(retrofit: Retrofit): NewsApi {

    val api = retrofit.create<RetrofitNewsApi>()

    return { query ->
        println("Fetching")
        api.fetchNews(query, "08a7e13902bf4cffab115365071e3850").articles
    }
}

object URLAdapter : TypeAdapter<URL> {
    override fun serialize(src: URL, typeOfSrc: Type?, context: JsonSerializationContext?) = JsonPrimitive(src.toExternalForm())
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?) = URL(json.asString)
}

object TitleAdapter : TypeAdapter<Title> {
    override fun serialize(src: Title, typeOfSrc: Type?, context: JsonSerializationContext?) = JsonPrimitive(src.value)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?) =
        Title(json.asString)
}

object AuthorAdapter : TypeAdapter<Author> {
    override fun serialize(src: Author, typeOfSrc: Type?, context: JsonSerializationContext?) = JsonPrimitive(src.value)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?) =
        Author(json.asString)
}

object DescriptionAdapter : TypeAdapter<Description> {
    override fun serialize(src: Description, typeOfSrc: Type?, context: JsonSerializationContext?) = JsonPrimitive(src.value)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?) =
        Description(json.asString)
}

private data class ArticlesResponse(val articles: List<Article>)

private interface RetrofitNewsApi {

    @GET("/v2/everything")
    suspend fun fetchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): ArticlesResponse

}