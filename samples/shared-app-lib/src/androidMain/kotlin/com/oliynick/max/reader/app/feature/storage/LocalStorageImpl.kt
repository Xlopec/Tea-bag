@file:Suppress("FunctionName")

@file:JvmName("AndroidLocalStorageImpl")

package com.oliynick.max.reader.app.feature.storage

import android.app.Application
import com.oliynick.max.reader.app.storage.AppDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

private const val DB_FILE_NAME = "app.db"

fun LocalStorage(
    application: Application
): LocalStorage =
    LocalStorage(AndroidSqliteDriver(AppDatabase.Schema, application, DB_FILE_NAME))