package com.max.reader.screens.settings

import com.max.reader.app.ScreenMessage

sealed class SettingsMessage : ScreenMessage()

object ToggleDarkMode : SettingsMessage()