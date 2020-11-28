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
                    eq("url", article.url.toExternalForm()),
                    Document.parse(gson.toJson(article)),
                    ReplaceOptions.createReplaceOptions(UpdateOptions().upsert(true))
                )
            }
        }
    }

    override suspend fun Env.removeFromFavorite(url: URL) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.findOneAndDelete(
                    eq("url", url.toExternalForm())
                )
            }
        }
    }


    override suspend fun Env.fetch(
        query: Query
    ): List<Article> = when (query.type) {
        Regular -> fetchFromEverything(query.input)
        Favorite -> fetchFavorite(query.input)
        Trending -> fetchTrending(query.input)
    }

    private suspend fun Env.fetchFromEverything(
        input: String,
    ) = toArticles(api.fetchFromEverything(API_KEY, input.toInputQueryMap()))

    private suspend fun Env.fetchTrending(
        input: String,
    ): List<Article> =
        toArticles(api.fetchTopHeadlines(API_KEY, application.countryCode, input.toInputQueryMap()))

    private suspend fun Env.fetchFavorite(
        input: String,
    ): List<Article> = coroutineScope {
        withContext(Dispatchers.IO) {
            collection
                .find(input.toTextSearchFilter())
                .map { gson.fromJson(it.toJson(), Article::class.java) }
                .into(ArrayList())
        }
    }

    private suspend fun Env.toArticles(
        response: ArticleResponse
    ) = response.articles.map { elem -> toArticle(elem) }

    private suspend fun Env.toArticle(
        element: ArticleElement
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

    private suspend fun Env.isFavorite(url: URL): Boolean =
        coroutineScope {
            withContext(Dispatchers.IO) {
                collection.countDocuments(eq("url", url.toExternalForm())) > 0
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
