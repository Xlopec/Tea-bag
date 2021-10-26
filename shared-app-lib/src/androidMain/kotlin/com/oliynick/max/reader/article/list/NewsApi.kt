package com.oliynick.max.reader.article.list

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import com.oliynick.max.reader.app.LocalStorage
import com.oliynick.max.reader.network.NewsApiImpl
import java.util.Locale.ENGLISH

fun NewsApi(
    application: Application
): NewsApi = NewsApiImpl(application.countryCode)

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