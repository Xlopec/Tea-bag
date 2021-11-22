package com.oliynick.max.reader.app.storage

import com.oliynick.max.tea.core.IO
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

class DeferredSettings(
    private val settingsProvider: () -> Settings
) {

    private val settings by lazy { settingsProvider() }

    suspend fun set(key: String, value: Boolean) =
        withContext(IO) { settings.putBoolean(key, value) }

    suspend fun get(key: String, default: Boolean = false) =
        withContext(IO) { settings.getBoolean(key, default) }

}