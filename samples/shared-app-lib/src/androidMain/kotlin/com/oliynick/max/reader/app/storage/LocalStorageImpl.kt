@file:Suppress("FunctionName")

@file:JvmName("AndroidLocalStorageImpl")

package com.oliynick.max.reader.app.storage

import android.app.Application
import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import com.squareup.sqldelight.android.AndroidSqliteDriver

private const val DB_FILE_NAME = "app.db"
private const val SHARED_PREFERENCES_NAME = "News_Reader"

// keep unused until 'sync with system settings' option is present
inline val Context.isDarkModeEnabled: Boolean
    get() = UI_MODE_NIGHT_YES == resources.configuration.uiMode and UI_MODE_NIGHT_MASK

fun LocalStorage(
    application: Application
): LocalStorage =
    LocalStorage(AndroidSqliteDriver(AppDatabase.Schema, application, DB_FILE_NAME))