@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.storage.AppDatabase
import com.oliynick.max.reader.app.storage.LocalStorage
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.domain.Url
import com.oliynick.max.reader.network.Page
import com.russhwolf.settings.AppleSettings
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import platform.Foundation.NSUserDefaults
import com.squareup.sqldelight.db.SqlDriver

private const val DB_FILE_NAME = "app.db"

fun LocalStorage(
    schema: SqlDriver.Schema = AppDatabase.Schema,
    dbName: String = DB_FILE_NAME,
    userDefaults: NSUserDefaults = NSUserDefaults()
): LocalStorage =
    LocalStorage(NativeSqliteDriver(schema, dbName), AppleSettings(userDefaults))

fun LocalStorageOld(): LocalStorage =
    object : LocalStorage {
        override suspend fun insertArticle(article: Article) {

        }

        override suspend fun deleteArticle(url: Url) {
        }

        override suspend fun findAllArticles(input: String): Page =
            Page(listOf(), false)

        override suspend fun isFavoriteArticle(url: Url): Boolean =
            false

        override suspend fun isDarkModeEnabled(): Boolean =
            false

        override suspend fun storeIsDarkModeEnabled(isEnabled: Boolean) {
        }

    }