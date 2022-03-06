package com.oliynick.max.reader.app.feature.storage

import com.oliynick.max.entities.shared.*
import com.oliynick.max.reader.app.IO
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.Title
import com.oliynick.max.reader.app.feature.article.list.Filter
import com.oliynick.max.reader.app.feature.article.list.FilterType
import com.oliynick.max.reader.app.feature.article.list.Page
import com.oliynick.max.reader.app.storage.AppDatabase
import com.oliynick.max.reader.app.storage.ArticlesQueries
import com.oliynick.max.reader.app.storage.RecentSearchesQueries
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.withContext

fun LocalStorage(
    driver: SqlDriver,
    settings: Settings = Settings(),
): LocalStorage = LocalStorageImpl(driver, settings)

private class LocalStorageImpl(
    driver: SqlDriver,
    private val settings: Settings,
) : LocalStorage {

    private companion object {
        private const val DarkModeEnabledKey = "DarkModeEnabledKey"
        private const val SyncWithSystemDarkModeEnabledKey = "SyncWithSystemDarkModeEnabledKey"
    }

    private val database = AppDatabase(driver)

    override suspend fun insertArticle(article: Article) = articlesQuery {
        // fixme there should be insert or replace option (upsert)
        transaction {
            with(article) {
                deleteArticle(url.toExternalValue())
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
                    is_favorite = isFavorite
                )
            }
        }
    }

    override suspend fun deleteArticle(url: Url) = articlesQuery {
        deleteArticle(url.toExternalValue())
    }

    override suspend fun findAllArticles(input: String) = articlesQuery {
        val wrappedInput = "%$input%"

        Page(
            findAllArticles(wrappedInput, wrappedInput, wrappedInput, ::dbModelToArticle)
                .executeAsList().toPersistentList()
        )
    }

    override suspend fun isFavoriteArticle(url: Url): Boolean = articlesQuery {
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

    override suspend fun storeRecentSearch(
        filter: Filter,
    ) = searchesQuery {
        transaction {
            insert(filter.input, filter.type.name, now().toMillis())
            deleteOutdated(filter.type.name, filter.type.name, 5)
        }
    }

    override suspend fun recentSearches(
        type: FilterType,
    ): ImmutableList<String> = searchesQuery {
        findAllByType(type.name) { value_, _, _ -> value_ }.executeAsList().toPersistentList()
    }

    private suspend inline fun <T> articlesQuery(
        crossinline block: ArticlesQueries.() -> T,
    ) = withContext(IO) {
        database.articlesQueries.run(block)
    }

    private suspend inline fun <T> searchesQuery(
        crossinline block: RecentSearchesQueries.() -> T,
    ) = withContext(IO) {
        database.recentSearchesQueries.run(block)
    }
}

private fun dbModelToArticle(
    url: String,
    title: String,
    author: String?,
    description: String?,
    urlToImage: String?,
    published: Long,
    // this unused arg is needed just to make function signatures match
    @Suppress("UNUSED_PARAMETER") savedOn: Long,
    isFavorite: Boolean,
): Article = Article(
    url = UrlFor(url),
    title = Title(title),
    author = author?.let(::Author),
    description = description?.let(::Description),
    urlToImage = urlToImage?.let(::UrlFor),
    published = fromMillis(published),
    isFavorite = isFavorite
)

private inline val SqlCursor.isFavorite: Boolean
    get() = getLong(6) != 0L
