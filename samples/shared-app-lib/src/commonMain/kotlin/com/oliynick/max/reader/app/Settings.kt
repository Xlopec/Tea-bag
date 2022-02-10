package com.oliynick.max.reader.app

@ImmutableType
data class Settings(
    val appDarkMode: Boolean,
    val systemDarkMode: Boolean,
    val syncWithSystemDarkMode: Boolean
) {
    val isInDarkMode: Boolean
        get() = if (syncWithSystemDarkMode) systemDarkMode else appDarkMode
}

fun Settings.updated(
    appDarkMode: Boolean = this.appDarkMode,
    systemDarkMode: Boolean = this.systemDarkMode,
    syncWithSystemDarkMode: Boolean = this.syncWithSystemDarkMode
) = copy(
    appDarkMode = appDarkMode,
    systemDarkMode = systemDarkMode,
    syncWithSystemDarkMode = syncWithSystemDarkMode
)