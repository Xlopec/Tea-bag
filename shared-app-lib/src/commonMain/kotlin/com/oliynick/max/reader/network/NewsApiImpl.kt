@file:Suppress("FunctionName")
@file:OptIn(InternalAPI::class)

package com.oliynick.max.reader.network

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.InternalException
import com.oliynick.max.reader.app.NetworkException
import com.oliynick.max.reader.app.datatypes.Either
import com.oliynick.max.reader.article.list.NewsApi
import com.oliynick.max.tea.core.IO
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.network.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

class NewsApiImpl(
    // todo refine this field
    private val countryCode: String
) : NewsApi {

    private val httpClient by lazy { HttpClient() }

    override suspend fun fetchFromEverything(
        input: String,
        currentSize: Int,
        resultsPerPage: Int
    ) = Either {
        httpClient.get(EverythingRequest(input, currentSize, resultsPerPage))
    }

    override suspend fun fetchTopHeadlines(
        input: String,
        currentSize: Int,
        resultsPerPage: Int
    ) = Either {
        httpClient.get(TopHeadlinesRequest(input, currentSize, resultsPerPage, countryCode))
    }
}

private fun HttpClient() = HttpClient {

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

private suspend inline fun Either(
    ifSuccess: () -> ArticleResponse
) = Either(ifSuccess, { it.toAppException() })

private suspend fun Throwable.toAppException(): AppException =
    wrap { raw ->
        when (raw) {
            is UnresolvedAddressException -> NetworkException(raw)
            is ClientRequestException -> raw.toAppException()
            else -> null
        }
    } ?: InternalException("An internal exception occurred", this)

private suspend inline fun Throwable.wrap(
    crossinline transform: suspend (Throwable) -> AppException?,
): AppException? =
    if (this is AppException) this
    else transform(this) ?: cause?.let { th -> transform(th) }

private suspend fun ClientRequestException.toAppException(): AppException =
    NetworkException(
        errorMessage() ?: toGenericExceptionDescription(),
        this
    )

@OptIn(ExperimentalSerializationApi::class)
private suspend fun ClientRequestException.errorMessage(
) = withContext(IO) {
    Json.decodeFromString<JsonElement>(response.readText())
        .jsonObject["message"]
        ?.jsonPrimitive
        ?.contentOrNull
}

private fun ClientRequestException.toGenericExceptionDescription() =
    "Server returned status code ${response.status.value}"

private fun NetworkException(
    cause: UnresolvedAddressException,
) = NetworkException("Network exception occurred, check connectivity", cause)

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