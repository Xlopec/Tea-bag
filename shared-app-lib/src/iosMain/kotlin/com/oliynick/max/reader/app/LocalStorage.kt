@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.storage.AppDatabase
import com.oliynick.max.reader.app.storage.LocalStorage
import com.russhwolf.settings.AppleSettings
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import platform.Foundation.NSUserDefaults

private const val DB_FILE_NAME = "app.db"

fun LocalStorage(
    schema: SqlDriver.Schema = AppDatabase.Schema,
    dbName: String = DB_FILE_NAME,
    userDefaults: NSUserDefaults = NSUserDefaults()
): LocalStorage =
    LocalStorage(NativeSqliteDriver(schema, dbName), AppleSettings(userDefaults))