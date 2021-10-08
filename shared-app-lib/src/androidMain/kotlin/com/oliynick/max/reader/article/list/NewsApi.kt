package com.oliynick.max.reader.article.list

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import com.oliynick.max.reader.network.NewsApiCommon
import com.oliynick.max.reader.network.Page
import java.util.Locale.ENGLISH

actual interface NewsApiEnv {
    val application: Application
}

fun NewsApiEnv(
    application: Application
): NewsApiEnv =
    object : NewsApiEnv {
        override val application: Application = application
    }

actual fun <Env : NewsApiEnv> NewsApi(): NewsApi<Env> = object : NewsApi<Env> {

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
    ): Page = impl.fetchTopHeadlines(input, currentSize, resultsPerPage, application.countryCode)

}

/*private class GsonSerializer(
    private val gson: Gson,
) : JsonSerializer {

    override fun write(data: Any, contentType: ContentType): OutgoingContent =
        TextContent(gson.toJson(data), contentType)

    override fun read(type: TypeInfo, body: Input): Any =
        gson.fromJson(body.readText(), type.reifiedType)
}*/

private inline val Application.countryCode: String
    get() = resources.configuration.countryCode

@Suppress("DEPRECATION")
private inline val Configuration.countryCode: String
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locales.get(0)?.country ?: ENGLISH.country
        } else {
            locale.country
        }
