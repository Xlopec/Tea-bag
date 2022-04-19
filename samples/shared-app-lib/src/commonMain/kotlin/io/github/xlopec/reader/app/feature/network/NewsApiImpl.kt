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
@file:OptIn(InternalAPI::class)

package io.github.xlopec.reader.app.feature.network

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.IO
import io.github.xlopec.reader.app.InternalException
import io.github.xlopec.reader.app.NetworkException
import io.github.xlopec.reader.app.domain.Query
import io.github.xlopec.reader.app.domain.SourceId
import io.github.xlopec.reader.app.feature.article.list.NewsApi
import io.github.xlopec.reader.app.feature.article.list.Paging
import io.github.xlopec.reader.app.feature.article.list.nextPage
import io.github.xlopec.tea.data.Either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.invoke
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.InternalAPI
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class NewsApiImpl(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
    private val countryCode: String,
) : NewsApi {

    private val httpClient by lazy { HttpClient(engine, LogLevel.ALL) }

    override suspend fun fetchFromEverything(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ) = Try {
        try {
        httpClient.get(EverythingRequest(query, sources, paging)).body<ArticleResponse>()
        } catch (th: Throwable) {
            // fixme crashes on IOS
            th.printStackTrace()
            ArticleResponse(0, emptyList())
        }
    }

    override suspend fun fetchTopHeadlines(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ) = Try {
        httpClient.get(TopHeadlinesRequest(query, sources, paging, countryCode)).body<ArticleResponse>()
    }

    override suspend fun fetchNewsSources() = Try {
        httpClient.get(SourcesRequest).body<SourcesResponse>()
    }
}

private fun HttpClient(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
    logLevel: LogLevel,
) = HttpClient(engine) {

    install(ContentNegotiation) {
        json(json = Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
            isLenient = true
        })
    }

    Logging {
        level = logLevel
    }
}

private suspend inline fun <T> Try(
    ifSuccess: () -> T,
) = Either(ifSuccess) { it.toAppException() }

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
        append("apiKey", ApiKey)
        appendPaging(paging)
        appendFiltering(query, sources)
    }
}

private fun TopHeadlinesRequest(
    query: Query?,
    sources: ImmutableSet<SourceId>,
    paging: Paging,
    countryCode: String,
) = HttpRequestBuilder(
    scheme = HTTPS.name,
    host = "newsapi.org",
    path = "/v2/top-headlines"
) {
    with(parameters) {
        append("apiKey", ApiKey)
        append("country", countryCode)
        appendPaging(paging)
        appendFiltering(query, sources)
    }
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
