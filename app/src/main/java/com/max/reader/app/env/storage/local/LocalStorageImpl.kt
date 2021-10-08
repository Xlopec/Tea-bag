package com.max.reader.app.env.storage.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.max.reader.R
import com.max.reader.ui.isDarkModeEnabled
import com.oliynick.max.reader.app.LocalStorage
import com.oliynick.max.reader.domain.*
import com.oliynick.max.reader.network.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Date

private const val DARK_MODE_ENABLED = "darkModeEnabled"

fun LocalStorage(
    context: Context,
): LocalStorage = object : LocalStorage {

    private val db by lazy { DbHelper(context).writableDatabase }
    private val sharedPreferences by lazy {
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    override suspend fun insertArticle(
        article: Article,
    ) {
        withContext(Dispatchers.IO) {
            db.insertWithOnConflict(
                TableName,
                null,
                article.toContentValues(),
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    override suspend fun deleteArticle(url: Url) {
        withContext(Dispatchers.IO) {
            db.delete(TableName, "$_Url = ?", arrayOf(url.toExternalForm()))
        }
    }

    override suspend fun findAllArticles(
        input: String,
    ): Page =
        withContext(Dispatchers.IO) {

            val selectionColumns =
                arrayOf(_Url, _Title, _Author, _Description, _UrlToImage, _Published, _IsFavorite)

            db.query(
                table = TableName,
                columns = selectionColumns + arrayOf("rowid"),
                selection = if (input.isEmpty()) null else selectionColumns.toLikeSelection(),
                selectionArgs = if (input.isEmpty()) null else "%$input%" * selectionColumns.size,
                orderBy = "rowid DESC"
            ).use(Cursor::toArticles)
        }.let(::Page)

    override suspend fun isFavoriteArticle(
        url: Url,
    ): Boolean =
        withContext(Dispatchers.IO) {
            db.query(
                table = TableName,
                columns = arrayOf(_Url),
                selection = "$_Url = ?",
                selectionArgs = arrayOf(url.toExternalForm())
            ).use { c -> c.count > 0 }
        }

    override suspend fun isDarkModeEnabled(): Boolean =
        withContext(Dispatchers.IO) {
            context.isDarkModeEnabled || sharedPreferences.getBoolean(DARK_MODE_ENABLED, false)
        }

    override suspend fun storeIsDarkModeEnabled(isEnabled: Boolean) =
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putBoolean(DARK_MODE_ENABLED, isEnabled).apply()
        }
}

private fun Array<out String>.toLikeSelection() =
    joinToString(separator = " OR ") { column -> "$column LIKE ?" }

private fun Article.toContentValues() =
    ContentValues().apply {
        put(_Url, url.toExternalForm())
        put(_Title, title.value)
        put(_Author, author?.value)
        put(_Description, description?.value)
        put(_UrlToImage, urlToImage?.toExternalForm())
        put(_Published, published.time)
        put(_IsFavorite, if (isFavorite) 1 else 0)
    }

private fun Cursor.toArticles(): List<Article> {
    val articles = mutableListOf<Article>()

    while (moveToNext()) {
        val url = URL(getString(_Url) ?: error("$_Url was null"))
        val title = Title(getString(_Title) ?: error("$_Title was null"))
        val author = getString(_Author)?.let(::Author)
        val description = getString(_Description)?.let(::Description)
        val urlToImage = getString(_UrlToImage)?.let(::URL)
        val published = Date(getLong(_Published))
        val isFavorite = getBoolean(_IsFavorite)

        articles += Article(url, title, author, description, urlToImage, published, isFavorite)
    }

    return articles
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
private fun SQLiteDatabase.query(
    table: String,
    columns: Array<out String>,
    selection: String?,
    selectionArgs: Array<out String>?,
    groupBy: String? = null,
    having: String? = null,
    orderBy: String? = null,
) = query(table, columns, selection, selectionArgs, groupBy, having, orderBy)

private fun Cursor.getString(
    name: String,
): String? = getString(getColumnIndex(name))

private fun Cursor.getLong(
    name: String,
): Long = getLong(getColumnIndex(name))

private fun Cursor.getBoolean(
    name: String,
): Boolean = getInt(getColumnIndex(name)) != 0

private infix operator fun String.times(
    times: Int,
) = Array(times) { this }