@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.storage

import com.oliynick.max.reader.app.storage.AppDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

private const val DB_FILE_NAME = "app.db"

fun LocalStorage(
    schema: SqlDriver.Schema = AppDatabase.Schema,
    dbName: String = DB_FILE_NAME,
): LocalStorage =
    LocalStorage(NativeSqliteDriver(schema, dbName))