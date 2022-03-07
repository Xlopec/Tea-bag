@file:Suppress("FunctionName")
@file:OptIn(InternalAPI::class)

package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.entities.shared.Url
import com.oliynick.max.entities.shared.datatypes.Either
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.IO
import com.oliynick.max.reader.app.InternalException
import com.oliynick.max.reader.app.NetworkException
import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.article.list.Paging
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.article.list.nextPage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.util.network.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import kotlin.jvm.JvmInline

internal class NewsApiImpl(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
    private val countryCode: String,
) : NewsApi {

    private val httpClient by lazy { HttpClient(engine) }

    override suspend fun fetchFromEverything(
        query: Query,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ) = Try {
        httpClient.get(EverythingRequest(query, sources, paging)).body<ArticleResponse>()
    }

    override suspend fun fetchTopHeadlines(
        query: Query,
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
) = HttpClient(engine) {

    install(ContentNegotiation) {
        json(json = Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
            isLenient = true
        })
    }

    Logging {
        level = LogLevel.ALL
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
    } ?: throw InternalException("An internal exception occurred", this)

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

data class Source(
    val id: SourceId,
    val name: SourceName,
    val description: SourceDescription?,
    val url: Url,
    val logo: Url,
)

@JvmInline
value class SourceName(
    val value: String,
)

@JvmInline
value class SourceDescription(
    val value: String,
)

@JvmInline
value class SourceId(
    val value: String,
)

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
    query: Query,
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
        appendFiltering(query.value, sources)
    }
}

private fun TopHeadlinesRequest(
    query: Query,
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
        appendFiltering(query.value, sources)
    }
}

private fun ParametersBuilder.appendPaging(
    paging: Paging,
) {
    append("page", paging.nextPage.toString())
    append("pageSize", paging.resultsPerPage.toString())
}

private fun ParametersBuilder.appendFiltering(
    input: String,
    sources: ImmutableSet<SourceId>,
) {
    if (sources.isNotEmpty()) {
        append("sources", sources.joinToString(transform = SourceId::value))
    }
    if (input.isNotEmpty()) {
        append("q", input)
    }
}

private const val ApiKey = "08a7e13902bf4cffab115365071e3850"