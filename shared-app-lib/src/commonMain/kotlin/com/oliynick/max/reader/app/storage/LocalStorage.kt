package com.oliynick.max.reader.app.storage

import com.oliynick.max.reader.app.LocalStorage
import com.oliynick.max.reader.article.list.Page
import com.oliynick.max.reader.domain.*
import com.oliynick.max.tea.core.IO
import com.russhwolf.settings.Settings
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use
import kotlinx.coroutines.withContext

fun LocalStorage(
    driver: SqlDriver,
    settings: Settings
): LocalStorage = LocalStorageImpl(driver, DeferredSettings { settings })

fun LocalStorage(
    driver: SqlDriver,
    settings: DeferredSettings
): LocalStorage = LocalStorageImpl(driver, settings)

private class LocalStorageImpl(
    driver: SqlDriver,
    private val settings: DeferredSettings
) : LocalStorage {

    private companion object {
        private const val DarkModeEnabledKey = "isDarkModeEnabled"
    }

    private val database = AppDatabase(driver)

    override suspend fun insertArticle(article: Article) = query {
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

    override suspend fun deleteArticle(url: Url) = query {
        deleteArticle(url.toExternalValue())
    }

    override suspend fun findAllArticles(input: String): Page = query {
        // todo check if it actually works
        val wrappedInput = "%$input%"

        Page(
            findAllArticles(wrappedInput, wrappedInput, wrappedInput, ::dbModelToArticle)
                .executeAsList()
        )
    }

    override suspend fun isFavoriteArticle(url: Url): Boolean = query {
        isFavoriteArticle(url.toExternalValue())
            .execute()
            .use { cursor -> cursor.next() && cursor.isFavorite }
    }

    override suspend fun isDarkModeEnabled(): Boolean =
        settings.get(DarkModeEnabledKey, false)

    override suspend fun storeIsDarkModeEnabled(isEnabled: Boolean) =
        settings.set(DarkModeEnabledKey, isEnabled)

    private suspend inline fun <T> query(
        crossinline block: ArticlesQueries.() -> T
    ) = withContext(IO) {
        database.articlesQueries.run(block)
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
    isFavorite: Boolean
): Article = Article(
    url = url.toUrl(),
    title = Title(title),
    author = author?.let(::Author),
    description = description?.let(::Description),
    urlToImage = urlToImage?.toUrl(),
    published = fromMillis(published),
    isFavorite = isFavorite
)

private inline val SqlCursor.isFavorite: Boolean
    get() = getLong(6) != 0L