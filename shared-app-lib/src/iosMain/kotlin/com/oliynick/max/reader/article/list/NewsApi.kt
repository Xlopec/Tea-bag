package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.PlatformEnv
import com.oliynick.max.reader.network.NewsApiCommon
import com.oliynick.max.reader.network.Page

actual fun <Env : NewsApiEnv> NewsApi(): NewsApi<Env> =
    object : NewsApi<Env> {

    private val impl = NewsApiCommon(
        /*HttpClient(CIO) {

            install(JsonFeature) {
                serializer = GsonSerializer(gson)
            }

            if (debug) {
                Logging {
                    level = LogLevel.ALL
                }
            }
        }*/
    )

    override suspend fun Env.fetchFromEverything(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Page = impl.fetchFromEverything(input, currentSize, resultsPerPage)

    override suspend fun Env.fetchTopHeadlines(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Page = impl.fetchTopHeadlines(input, currentSize, resultsPerPage, "EN")

}

actual interface NewsApiEnv

fun NewsApiEnv(
): NewsApiEnv =
    object : NewsApiEnv {

    }

actual fun NewsApiEnv(platformEnv: PlatformEnv): NewsApiEnv =
    object : NewsApiEnv {

    }