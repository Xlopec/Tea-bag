package com.oliynick.max.reader.app

@ImmutableType
data class Settings(
    val userDarkModeEnabled: Boolean,
    val systemDarkModeEnabled: Boolean,
    val syncWithSystemDarkModeEnabled: Boolean
) {
    val appDarkModeEnabled: Boolean
        get() = if (syncWithSystemDarkModeEnabled) systemDarkModeEnabled else userDarkModeEnabled
}

fun Settings.updated(
    userDarkModeEnabled: Boolean = this.userDarkModeEnabled,
    systemDarkModeEnabled: Boolean = this.systemDarkModeEnabled,
    syncWithSystemDarkModeEnabled: Boolean = this.syncWithSystemDarkModeEnabled
) = copy(
    userDarkModeEnabled = userDarkModeEnabled,
    systemDarkModeEnabled = systemDarkModeEnabled,
    syncWithSystemDarkModeEnabled = syncWithSystemDarkModeEnabled
)
