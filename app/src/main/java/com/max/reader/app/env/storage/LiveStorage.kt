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

package com.max.reader.app.env.storage

import android.app.Application
import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.local.HasMongoCollection
import com.max.reader.app.env.storage.network.ArticleElement
import com.max.reader.app.env.storage.network.ArticleResponse
import com.max.reader.app.env.storage.network.NewsApi
import com.max.reader.domain.Article
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.text
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.TextSearchOptions
import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@Deprecated("wait until it'll be fixed")
fun <Env> Storage(): Storage<Env> where Env : HasMongoCollection,
                                        Env : NewsApi,
                                        Env : HasAppContext,
                                        Env : HasGson = object : Storage<Env> {

    override suspend fun Env.addToFavorite(article: Article) {
        withContext(Dispatchers.IO) {
            collection.replaceOne(
                article.url.toEqPredicate(),
                Document.parse(gson.toJson(article)),
                ReplaceOptions.createReplaceOptions(UpdateOptions().upsert(true))
            )
        }
    }

    override suspend fun Env.removeFromFavorite(url: URL) {
        withContext(Dispatchers.IO) {
            collection.findOneAndDelete(url.toEqPredicate())
        }
    }

    override suspend fun Env.fetch(
        query: Query,
        currentSize: Int,
        resultsPerPage: Int,
    ): Storage.Page = when (query.type) {
        Regular -> fetchFromEverything(query.input, currentSize, resultsPerPage)
        Favorite -> fetchFavorite(query.input)
        Trending -> fetchTrending(query.input, currentSize, resultsPerPage)
    }

    private suspend fun Env.fetchFromEverything(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Storage.Page =
        toPage(
            fetchFromEverything(
                (currentSize / resultsPerPage) + 1,
                resultsPerPage,
                input.toInputQueryMap()
            ),
            currentSize,
            resultsPerPage
        )

    private suspend fun Env.toPage(
        response: ArticleResponse,
        currentSize: Int,
        resultsPerPage: Int,
    ): Storage.Page {
        val (total, results) = response
        val skip = currentSize % resultsPerPage

        val tail = if (skip == 0 || results.isEmpty()) results
        else results.subList(skip, results.size)

        return Storage.Page(toArticles(tail), currentSize + tail.size < total)
    }

    private suspend fun Env.fetchTrending(
        input: String,
        currentSize: Int,
        resultsPerPage: Int,
    ): Storage.Page =
        toPage(
            fetchTopHeadlines(
                application.countryCode,
                (currentSize / resultsPerPage) + 1,
                resultsPerPage,
                input.toInputQueryMap()
            ),
            currentSize,
            resultsPerPage
        )

    private suspend fun Env.fetchFavorite(
        input: String,
    ): Storage.Page =
        withContext(Dispatchers.IO) {
            Storage.Page(
                articles = collection
                    .find(input.toTextSearchFilter())
                    .map { document -> gson.fromJson(document.toJson(), Article::class.java) }
                    .into(ArrayList()),
                hasMore = false
            )
        }

    private suspend fun Env.toArticles(
        articles: Iterable<ArticleElement>,
    ) = articles.map { elem -> toArticle(elem) }

    private suspend fun Env.toArticle(
        element: ArticleElement,
    ) =
        Article(
            url = element.url,
            title = element.title,
            author = element.author,
            description = element.description,
            urlToImage = element.urlToImage,
            isFavorite = isFavorite(element.url),
            published = element.publishedAt
        )

    private suspend fun Env.isFavorite(
        url: URL,
    ): Boolean =
        withContext(Dispatchers.IO) {
            collection.countDocuments(url.toEqPredicate()) > 0
        }
}

private inline val Application.countryCode: String
    get() = resources.configuration.countryCode

@Suppress("DEPRECATION")
private inline val Configuration.countryCode: String
    get() =
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            locales.get(0)?.country ?: Locale.ENGLISH.country
        } else {
            locale.country
        }

private fun String.toTextSearchFilter(): Bson =
    if (isEmpty()) BsonDocument() else text(this, TextSearchOptions)

private fun String.toInputQueryMap(): Map<String, String> =
    if (isEmpty()) emptyMap() else mapOf("q" to this)

private val TextSearchOptions = TextSearchOptions()
    .apply { caseSensitive(false) }

private fun URL.toEqPredicate(): Bson =
    eq("url", toExternalForm())
