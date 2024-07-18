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

package io.github.xlopec.reader.app.feature.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.SqlPreparedStatement
import com.squareup.sqldelight.db.use
import io.github.xlopec.reader.app.feature.article.list.Page
import io.github.xlopec.reader.app.misc.mapNotNullToPersistentList
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Author
import io.github.xlopec.reader.app.model.Description
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.SourceId
import io.github.xlopec.reader.app.model.Title
import io.github.xlopec.reader.app.storage.AppDatabase
import io.github.xlopec.reader.app.storage.ArticlesQueries
import io.github.xlopec.reader.app.storage.FiltersQueries
import io.github.xlopec.reader.app.storage.RecentSearchesQueries
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.UrlFor
import io.github.xlopec.tea.data.fromMillis
import io.github.xlopec.tea.data.now
import io.github.xlopec.tea.data.toExternalValue
import io.github.xlopec.tea.data.toMillis
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

fun LocalStorage(
    driver: SqlDriver,
    settings: Settings = Settings(),
): LocalStorage = LocalStorageImpl(driver, settings)

private class LocalStorageImpl(
    private val driver: SqlDriver,
    private val settings: Settings,
) : LocalStorage {

    private companion object {
        private const val DarkModeEnabledKey = "DarkModeEnabledKey"
        private const val SyncWithSystemDarkModeEnabledKey = "SyncWithSystemDarkModeEnabledKey"
    }

    private val database = AppDatabase(driver)

    override suspend fun insertArticle(
        article: Article,
    ) = articlesQuery {
        transaction {
            with(article) {
                insertArticle(
                    url = url.toExternalValue(),
                    title = title.value,
                    author = author?.value,
                    description = description?.value,
                    url_to_image = urlToImage?.toExternalValue(),
                    // I don't expect performance issues here as we aren't going
                    // to store thousands of articles in a row
                    saved_on = now().toMillis(),
                    published = published.toMillis(),
                    is_favorite = isFavorite,
                    source = article.source?.value
                )
            }
        }
    }

    override suspend fun deleteArticle(
        url: Url,
    ) = articlesQuery {
        deleteArticle(url.toExternalValue())
    }

    override suspend fun findAllArticles(
        filter: Filter,
    ) = articlesQuery {
        Page(driver.executeQuery(filter).toPersistentList(SqlCursor::toArticle))
    }

    override suspend fun isFavoriteArticle(
        url: Url,
    ): Boolean = articlesQuery {
        isFavoriteArticle(url.toExternalValue())
            .execute()
            .use { cursor -> cursor.next() && cursor.isFavorite }
    }

    override suspend fun isDarkModeEnabled(): Boolean = articlesQuery {
        settings[DarkModeEnabledKey, false]
    }

    override suspend fun isSyncWithSystemDarkModeEnabled(): Boolean = articlesQuery {
        settings[SyncWithSystemDarkModeEnabledKey, false]
    }

    override suspend fun storeDarkModePreferences(
        appDarkMode: Boolean,
        syncWithSystemDarkMode: Boolean,
    ) = articlesQuery {
        settings[DarkModeEnabledKey] = appDarkMode
        settings[SyncWithSystemDarkModeEnabledKey] = syncWithSystemDarkMode
    }

    override suspend fun storeFilter(
        filter: Filter,
        storeSuggestionsLimit: UInt,
        storeSourcesLimit: UInt,
    ) {
        val type = filter.type.ordinal.toLong()

        filtersQuery {
            transaction {
                insertFilter(type, filter.query?.value)
                deleteSources(type)
                filter.sources.take(storeSourcesLimit.toInt()).forEach { sourceId ->
                    insertSource(sourceId.value, type)
                }
            }
        }

        if (filter.query != null) {
            searchesQuery {
                insert(filter.query.value, type, now().toMillis())
                deleteOutdated(type, type, storeSuggestionsLimit.toLong())
            }
        }
    }

    override suspend fun findFilter(type: FilterType): Filter = filtersQuery {
        Filter(type, findInputByType(type), findSourcesByType(type))
    }

    override suspend fun recentSearches(
        type: FilterType,
    ): ImmutableList<Query> = searchesQuery {
        findAllByType(type.ordinal.toLong()) { value_, _, _ -> value_ }
            .executeAsList()
            .mapNotNullToPersistentList(Query::of)
    }

    override suspend fun deleteRecentSearch(type: FilterType, query: Query) = searchesQuery {
        delete(type.ordinal.toLong(), query.value)
    }

    private suspend inline fun <T> articlesQuery(
        crossinline block: ArticlesQueries.() -> T,
    ) = withContext(Dispatchers.IO) {
        database.articlesQueries.run(block)
    }

    private suspend inline fun <T> searchesQuery(
        crossinline block: suspend RecentSearchesQueries.() -> T,
    ) = withContext(Dispatchers.IO) {
        database.recentSearchesQueries.run { block() }
    }

    private suspend inline fun <T> filtersQuery(
        crossinline block: suspend FiltersQueries.() -> T,
    ) = withContext(Dispatchers.IO) {
        database.filtersQueries.run { block() }
    }
}

private fun FiltersQueries.findSourcesByType(
    type: FilterType,
) = findAllSourcesByType(type.ordinal.toLong()) { source, _ -> SourceId(source) }
    .executeAsList()
    .toPersistentSet()

private fun FiltersQueries.findInputByType(
    type: FilterType,
) = findFilterByType(type.ordinal.toLong()) { _, input -> input ?: "" }
    .executeAsOneOrNull()
    .let(Query.Companion::of)

private fun SqlCursor.toArticle() =
    Article(
        url = UrlFor(getString(0)!!),
        title = Title(getString(1)!!),
        author = getString(2)?.let(::Author),
        description = getString(3)?.let(::Description),
        urlToImage = getString(4)?.let(::UrlFor),
        published = fromMillis(getLong(5)!!),
        isFavorite = isFavorite,
        source = getString(8)?.let(::SourceId)
    )

private fun <T> SqlCursor.toPersistentList(
    mapper: SqlCursor.() -> T,
) = persistentListOf<T>().builder().apply {
    use { cursor ->
        while (cursor.next()) {
            add(mapper.invoke(this@toPersistentList))
        }
    }
}.build()

private fun createSqlArguments(
    count: Int,
): String {
    if (count == 0) return "()"

    return buildString(count + 2) {
        append("(?")
        repeat(count - 1) {
            append(",?")
        }
        append(')')
    }
}

private val Filter.parametersCount: Int
    get() = if (query?.value == null) 0 else 3 + sources.size

private fun Filter.toSqlQuery(): String {
    val inputFilter = query?.value?.let {
        " (title LIKE ? OR author LIKE ? OR description LIKE ?) "
    }
    val sourcesFilter = sources.size.takeIf { it > 0 }?.let {
        " (source IN ${createSqlArguments(count = it)} OR source IS NULL)"
    }

    val filteringClause = when {
        inputFilter != null && sourcesFilter != null -> "WHERE $inputFilter AND $sourcesFilter"
        inputFilter != null -> "WHERE $inputFilter"
        sourcesFilter != null -> "WHERE $sourcesFilter"
        else -> ""
    }

    return "SELECT * FROM Articles $filteringClause ORDER BY saved_on DESC"
}

private fun SqlPreparedStatement.bindValues(
    filter: Filter,
) {
    val boundInput = filter.query?.value?.let { "%$it%" }

    if (boundInput != null) {
        bindString(1, boundInput)
        bindString(2, boundInput)
        bindString(3, boundInput)
    }

    val shift = if (boundInput == null) 0 else 3

    filter.sources.map(SourceId::value).forEachIndexed { index, source_ ->
        bindString(index + 1 + shift, source_)
    }
}

private fun SqlDriver.executeQuery(
    filter: Filter,
) = executeQuery(null, filter.toSqlQuery(), filter.parametersCount) {
    bindValues(filter)
}

private inline val SqlCursor.isFavorite: Boolean
    get() = getLong(7) != 0L
