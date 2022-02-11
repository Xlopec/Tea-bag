package com.oliynick.max.reader.app.feature.article.list

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import com.oliynick.max.reader.app.feature.network.NewsApiImpl
import io.ktor.client.engine.cio.*
import java.util.Locale.ENGLISH

fun NewsApi(
    application: Application
): NewsApi = NewsApiImpl(CIO, application.countryCode)

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