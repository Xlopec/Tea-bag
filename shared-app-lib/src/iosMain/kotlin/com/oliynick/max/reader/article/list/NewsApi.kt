package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.network.NewsApiImpl
import io.ktor.client.engine.ios.*

fun NewsApi(): NewsApi = NewsApiImpl(Ios, "EN")