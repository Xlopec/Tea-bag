package com.oliynick.max.reader.app.storage

import com.oliynick.max.reader.app.LocalStorage
import com.oliynick.max.reader.domain.*
import com.oliynick.max.reader.network.Page
import com.oliynick.max.tea.core.IO
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.withContext

fun LocalStorage(
    driver: SqlDriver,
    settings: Settings
): LocalStorage = LocalStorageImpl(driver, settings)

private class LocalStorageImpl(
    driver: SqlDriver,
    private val settings: Settings
) : LocalStorage {

    private companion object {
        private const val DarkModeEnabledKey = "isDarkModeEnabled"
    }

    private val database = AppDatabase(driver)
    private val queries = database.articlesQueries

    override suspend fun insertArticle(article: Article) = withContext(IO) {
        with(article) {
            queries.insertArticle(
                url = url.toExternalValue(),
                title = title.value,
                author = author?.value,
                description = description?.value,
                url_to_image = urlToImage?.toExternalValue(),
                published = published.toMillis(),
                is_favorite = isFavorite
            )
        }
    }

    override suspend fun deleteArticle(url: Url) = withContext(IO) {
        queries.deleteArticle(url.toExternalValue())
    }

    override suspend fun findAllArticles(input: String): Page = withContext(IO) {
        val wrappedInput = "%$input%"
        Page(queries.findAllArticles(wrappedInput, wrappedInput, wrappedInput, ::dbModelToArticle).executeAsList())
    }

    override suspend fun isFavoriteArticle(url: Url): Boolean = withContext(IO) {
        queries.isFavoriteArticle(url.toExternalValue(), ::dbModelToArticle)
            .executeAsOneOrNull()?.isFavorite == true
    }

    override suspend fun isDarkModeEnabled(): Boolean = withContext(IO) {
        settings[DarkModeEnabledKey, false]
    }

    override suspend fun storeIsDarkModeEnabled(isEnabled: Boolean) = withContext(IO) {
        settings[DarkModeEnabledKey] = isEnabled
    }

}

private fun dbModelToArticle(
    url: String,
    title: String,
    author: String?,
    description: String?,
    urlToImage: String?,
    published: Long,
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