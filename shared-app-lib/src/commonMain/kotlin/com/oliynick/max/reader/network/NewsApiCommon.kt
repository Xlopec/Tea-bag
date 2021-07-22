@file:Suppress("FunctionName")
@file:OptIn(InternalAPI::class)

package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.Article
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.json.Json

class NewsApiCommon {

    private val httpClient = HttpClient {

        install(JsonFeature) {
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            }
            serializer = KotlinxSerializer(json)
        }

        Logging {
            level = LogLevel.ALL
        }
    }

    /*private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = this@NewsApiCommon.serializer
        }

        if (debug) {
            Logging {
                level = LogLevel.ALL
            }
        }
    }*/

    @Throws(Exception::class)
    // todo try to make it return Either<Page, NetworkException>
    suspend fun fetchFromEverything(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Page =
        toPage(
            httpClient.get(EverythingRequest(input, currentSize, resultsPerPage)),
            currentSize,
            resultsPerPage
        )

    @Throws(Exception::class)
    // todo try to make it return Either<Page, NetworkException>
    suspend fun fetchTopHeadlines(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
        countryCode: String,
    ): Page =
        toPage(
            httpClient.get(TopHeadlinesRequest(input, currentSize, resultsPerPage, countryCode)),
            currentSize,
            resultsPerPage
        )

}

private /*suspend*/ fun toPage(
    response: ArticleResponse,
    currentSize: Int,
    resultsPerPage: Int,
): Page {
    val (total, results) = response
    val skip = currentSize % resultsPerPage

    val tail = if (skip == 0 || results.isEmpty()) results
    else results.subList(skip, results.size)

    return Page(toArticles(tail), currentSize + tail.size < total)
}

private /*suspend*/ fun toArticles(
    articles: Iterable<ArticleElement>,
) = articles.map { elem -> toArticle(elem) }

private /*suspend*/ fun toArticle(
    element: ArticleElement,
) =
    // todo remove conversion
    Article(
        url = element.url,
        title = element.title,
        author = element.author,
        description = element.description,
        urlToImage = element.urlToImage,
        isFavorite = false,//isFavoriteArticle(element.url),
        published = element.publishedAt
    )

private fun EverythingRequest(
    input: String,
    currentSize: Int,
    resultsPerPage: Int,
) = HttpRequestBuilder(
    scheme = URLProtocol.HTTPS.name,
    host = "newsapi.org",
    path = "/v2/everything"
) {
    with(parameters) {
        append("apiKey", ApiKey)
        append("page", ((currentSize / resultsPerPage) + 1).toString())
        append("pageSize", resultsPerPage.toString())

        input.toInputQueryMap()
            .forEach { (k, v) ->
                append(k, v)
            }
    }
}

private fun TopHeadlinesRequest(
    input: String,
    currentSize: Int,
    resultsPerPage: Int,
    countryCode: String,
) = HttpRequestBuilder(
    scheme = URLProtocol.HTTPS.name,
    host = "newsapi.org",
    path = "/v2/top-headlines"
) {
    with(parameters) {
        append("apiKey", ApiKey)
        append("page", ((currentSize / resultsPerPage) + 1).toString())
        append("pageSize", resultsPerPage.toString())
        append("country", countryCode)

        input.toInputQueryMap()
            .forEach { (k, v) ->
                append(k, v)
            }
    }
}

private fun String.toInputQueryMap(): Map<String, String> =
    if (isEmpty()) emptyMap() else mapOf("q" to this)

private const val ApiKey = "08a7e13902bf4cffab115365071e3850"