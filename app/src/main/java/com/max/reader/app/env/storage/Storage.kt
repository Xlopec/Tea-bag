package com.max.reader.app.env.storage

import com.max.reader.domain.Article
import com.max.reader.screens.article.list.LoadCriteria
import java.net.URL

interface Storage<Env> {

    suspend fun Env.addToFavorite(article: Article)

    suspend fun Env.removeFromFavorite(url: URL)

    suspend fun Env.fetchFavorite(): List<Article>

    suspend fun Env.fetch(
        criteria: LoadCriteria.Query
    ): List<Article>

    suspend fun Env.fetchTrending(): List<Article>

}