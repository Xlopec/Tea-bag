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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app.feature.network

import arrow.core.Either
import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.InternalException
import io.github.xlopec.reader.app.NetworkException
import io.github.xlopec.reader.app.feature.article.list.NewsApi
import io.github.xlopec.reader.app.feature.article.list.Paging
import io.github.xlopec.reader.app.feature.article.list.nextPage
import io.github.xlopec.reader.app.model.Country
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.SourceId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.network.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class NewsApiImpl(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
    private val country: Country,
) : NewsApi {

    private val httpClient by lazy { HttpClient(engine, LogLevel.ALL) }

    override suspend fun fetchFromEverything(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ) = TryRequest {
        httpClient.get(EverythingRequest(query, sources, paging)).body<ArticleResponse>()
    }

    override suspend fun fetchTopHeadlines(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ) = TryRequest {
        httpClient.get(TopHeadlinesRequest(query, sources, paging, country.takeIf { sources.isEmpty() })).body<ArticleResponse>()
    }

    override suspend fun fetchNewsSources() = TryRequest {
        httpClient.get(SourcesRequest).body<SourcesResponse>()
    }
}

private fun HttpClient(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
    logLevel: LogLevel,
) = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
                isLenient = true
            }
        )
    }

    Logging {
        logger = Logger.SIMPLE
        level = logLevel
    }
}

private suspend inline fun <T> TryRequest(
    ifSuccess: () -> T,
) = Either.catch(ifSuccess).mapLeft { it.toAppException() }

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
    if (this is AppException) {
        this
    } else {
        transform(this) ?: cause?.let { th -> transform(th) }
    }

private suspend fun ClientRequestException.toAppException(): AppException =
    NetworkException(
        errorMessage() ?: toGenericExceptionDescription(),
        this
    )

private suspend fun ClientRequestException.errorMessage() = withContext(Dispatchers.IO) {
    Json.decodeFromString<JsonElement>(response.bodyAsText())
        .jsonObject["message"]
        ?.jsonPrimitive
        ?.contentOrNull
}

private fun ClientRequestException.toGenericExceptionDescription() =
    "Server returned status code ${response.status.value}"

private fun NetworkException(
    cause: UnresolvedAddressException,
) = NetworkException("Network exception occurred, check connectivity", cause)

private val SourcesRequest = HttpRequestBuilder(
    scheme = HTTPS.name,
    host = "newsapi.org",
    path = "/v2/top-headlines/sources"
) {
    with(parameters) {
        append("apiKey", ApiKey)
    }
}

private fun EverythingRequest(
    query: Query?,
    sources: ImmutableSet<SourceId>,
    paging: Paging,
) = HttpRequestBuilder(
    scheme = HTTPS.name,
    host = "newsapi.org",
    path = "/v2/everything"
) {
    with(parameters) {
        appendApiKey(ApiKey)
        appendPaging(paging)
        appendFiltering(query, sources)
    }
}

private fun TopHeadlinesRequest(
    query: Query?,
    sources: ImmutableSet<SourceId>,
    paging: Paging,
    country: Country?,
) = HttpRequestBuilder(
    scheme = HTTPS.name,
    host = "newsapi.org",
    path = "/v2/top-headlines"
) {
    with(parameters) {
        appendApiKey(ApiKey)
        country?.also { appendCountry(it) }
        appendPaging(paging)
        appendFiltering(query, sources)
    }
}

private fun ParametersBuilder.appendApiKey(
    apiKey: String
) {
    append("apiKey", apiKey)
}

private fun ParametersBuilder.appendCountry(
    country: Country
) {
    append("country", country.code)
}

private fun ParametersBuilder.appendPaging(
    paging: Paging,
) {
    append("page", paging.nextPage.toString())
    append("pageSize", paging.resultsPerPage.toString())
}

private fun ParametersBuilder.appendFiltering(
    query: Query?,
    sources: ImmutableSet<SourceId>,
) {
    if (sources.isNotEmpty()) {
        append("sources", sources.joinToString(transform = SourceId::value))
    }
    if (query != null) {
        append("q", query.value)
    }
}

private const val ApiKey = "08a7e13902bf4cffab115365071e3850"
