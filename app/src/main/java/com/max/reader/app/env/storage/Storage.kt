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