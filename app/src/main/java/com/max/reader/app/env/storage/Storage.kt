package com.max.reader.app.env.storage

import com.max.reader.domain.Article
import com.max.reader.screens.article.list.Query
import java.net.URL

interface Storage<Env> {

    data class Page(
        val articles: List<Article>,
        val hasMore: Boolean
    )

    suspend fun Env.addToFavorite(article: Article)

    suspend fun Env.removeFromFavorite(url: URL)

    suspend fun Env.fetch(
        query: Query,
        currentSize: Int,
        resultsPerPage: Int
    ): Page

}