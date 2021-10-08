/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.max.reader.app.env.storage.network

import android.content.res.Configuration
import android.os.Build
import com.google.gson.Gson
import com.oliynick.max.reader.app.LocalStorage
import com.oliynick.max.reader.article.details.ArticleDetailsEnv
import com.oliynick.max.reader.network.NewsApiCommon
import com.oliynick.max.reader.network.Page
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.core.*
import java.util.Locale.ENGLISH

interface NewsApi<Env> {

    suspend fun Env.fetchFromEverything(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Page

    suspend fun Env.fetchTopHeadlines(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Page
}

fun <Env> NewsApi(
    gson: Gson,
    debug: Boolean,
): NewsApi<Env> where Env : LocalStorage,
                      Env : ArticleDetailsEnv = object : NewsApi<Env> {

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
    ): Page = impl.fetchTopHeadlines(input, currentSize, resultsPerPage, countryCode)

}

private class GsonSerializer(
    private val gson: Gson,
) : JsonSerializer {

    override fun write(data: Any, contentType: ContentType): OutgoingContent =
        TextContent(gson.toJson(data), contentType)

    override fun read(type: TypeInfo, body: Input): Any =
        gson.fromJson(body.readText(), type.reifiedType)
}

private inline val ArticleDetailsEnv.countryCode: String
    get() = application.resources.configuration.countryCode

@Suppress("DEPRECATION")
private inline val Configuration.countryCode: String
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locales.get(0)?.country ?: ENGLISH.country
        } else {
            locale.country
        }
