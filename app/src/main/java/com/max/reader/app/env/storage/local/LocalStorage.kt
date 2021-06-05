/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.max.reader.app.env.storage.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import com.max.reader.domain.Article
import com.max.reader.domain.Author
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.*

interface LocalStorage {

    suspend fun insertArticle(
        article: Article,
    )

    suspend fun deleteArticle(
        url: URL,
    )

    suspend fun findAllArticles(
        input: String,
    ): List<Article>

    suspend fun isFavoriteArticle(
        url: URL,
    ): Boolean
}

fun LocalStorage(
    context: Context,
): LocalStorage = object : LocalStorage {

    private val db by lazy { DbHelper(context).writableDatabase }

    override suspend fun insertArticle(
        article: Article,
    ) {
        withContext(Dispatchers.IO) {
            db.insertWithOnConflict(TableName, null, article.toContentValues(), CONFLICT_REPLACE)
        }
    }

    override suspend fun deleteArticle(url: URL) {
        withContext(Dispatchers.IO) {
            db.delete(TableName, "$_Url = ?", arrayOf(url.toExternalForm()))
        }
    }

    override suspend fun findAllArticles(
        input: String
    ): List<Article> =
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
        }

    override suspend fun isFavoriteArticle(
        url: URL,
    ): Boolean =
        withContext(Dispatchers.IO) {
            db.query(
                table = TableName,
                columns = arrayOf(_Url),
                selection = "$_Url = ?",
                selectionArgs = arrayOf(url.toExternalForm())
            ).use { c -> c.count > 0 }
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
    name: String
): String? = getString(getColumnIndex(name))

private fun Cursor.getLong(
    name: String
): Long = getLong(getColumnIndex(name))

private fun Cursor.getBoolean(
    name: String
): Boolean = getInt(getColumnIndex(name)) != 0

private infix operator fun String.times(
    times: Int,
) = Array(times) { this }
