package com.oliynick.max.reader.app.feature.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.oliynick.max.reader.app.Settings as AppSettings

fun Settings.appSettings(
    isSystemInDarkMode: Boolean
) = AppSettings(
    userDarkModeEnabled = this[DarkModeEnabledKey, false],
    systemDarkModeEnabled = isSystemInDarkMode,
    syncWithSystemDarkModeEnabled = this[SyncWithSystemDarkModeEnabledKey, false],
)

val Settings.appSettings: AppSettings
    get() = AppSettings(
        userDarkModeEnabled = this[DarkModeEnabledKey, false],
        systemDarkModeEnabled = isSystemInDarkMode(),
        syncWithSystemDarkModeEnabled = this[SyncWithSystemDarkModeEnabledKey, false],
    )