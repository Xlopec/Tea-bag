package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.feature.network.NewsApiImpl
import io.ktor.client.engine.darwin.*
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

fun NewsApi(): NewsApi = NewsApiImpl(Darwin, NSLocale.currentLocale.countryCode ?: "en")