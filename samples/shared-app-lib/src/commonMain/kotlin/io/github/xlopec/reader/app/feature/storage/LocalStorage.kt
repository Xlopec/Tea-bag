/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.reader.app.feature.storage

import io.github.xlopec.reader.app.feature.article.list.Page
import io.github.xlopec.reader.app.feature.filter.FiltersState.Companion.StoreSuggestionsLimit
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.Filter.Companion.StoreSourcesLimit
import io.github.xlopec.reader.app.model.FilterType
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.tea.data.Url
import kotlinx.collections.immutable.ImmutableList

interface LocalStorage {

    suspend fun insertArticle(
        article: Article,
    )

    suspend fun deleteArticle(
        url: Url,
    )

    suspend fun findAllArticles(
        filter: Filter,
    ): Page<Article>

    suspend fun isFavoriteArticle(
        url: Url,
    ): Boolean

    suspend fun isDarkModeEnabled(): Boolean

    suspend fun isSyncWithSystemDarkModeEnabled(): Boolean

    suspend fun storeDarkModePreferences(
        appDarkMode: Boolean,
        syncWithSystemDarkMode: Boolean,
    )

    suspend fun storeFilter(
        filter: Filter,
        storeSuggestionsLimit: UInt = StoreSuggestionsLimit,
        storeSourcesLimit: UInt = StoreSourcesLimit,
    )

    suspend fun findFilter(
        type: FilterType
    ): Filter

    suspend fun recentSearches(
        type: FilterType
    ): ImmutableList<Query>

    suspend fun deleteRecentSearch(
        type: FilterType,
        query: Query
    )
}
