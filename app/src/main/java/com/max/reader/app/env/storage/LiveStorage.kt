/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.max.reader.app.env.storage

import android.app.Application
import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.local.HasMongoCollection
import com.max.reader.app.env.storage.network.ArticleElement
import com.max.reader.app.env.storage.network.ArticleResponse
import com.max.reader.app.env.storage.network.HasNewsApi
import com.max.reader.domain.Article
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.text
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.TextSearchOptions
import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson
import java.net.URL

private const val API_KEY = "08a7e13902bf4cffab115365071e3850"

@Deprecated("wait until it'll be fixed")
fun <Env> Storage(): Storage<Env> where Env : HasMongoCollection,
                                        Env : HasNewsApi,
                                        Env : HasAppContext,
                                        Env : HasGson = object : Storage<Env> {

    override suspend fun Env.addToFavorite(article: Article) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.replaceOne(
                    eqUrl(article.url),
                    Document.parse(gson.toJson(article)),
                    ReplaceOptions.createReplaceOptions(UpdateOptions().upsert(true))
                )
            }
        }
    }

    override suspend fun Env.removeFromFavorite(url: URL) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.findOneAndDelete(eqUrl(url))
            }
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
            api.fetchFromEverything(
                API_KEY,
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
            api.fetchTopHeadlines(
                API_KEY,
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
        coroutineScope {
            withContext(Dispatchers.IO) {
                Storage.Page(
                    articles = collection
                        .find(input.toTextSearchFilter())
                        .map { document -> gson.fromJson(document.toJson(), Article::class.java) }
                        .into(ArrayList()),
                    hasMore = false
                )
            }
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
        url: URL
    ): Boolean =
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.countDocuments(eqUrl(url)) > 0
            }
        }

    private inline val Application.countryCode: String
        get() = resources.configuration.locale.country
}

private fun String.toTextSearchFilter(): Bson =
    if (isEmpty()) BsonDocument() else text(this, TextSearchOptions)

private fun String.toInputQueryMap(): Map<String, String> =
    if (isEmpty()) emptyMap() else mapOf("q" to this)

private val TextSearchOptions = TextSearchOptions()
    .apply { caseSensitive(false) }

private fun eqUrl(
    url: URL
) = eq("url", url.toExternalForm())
