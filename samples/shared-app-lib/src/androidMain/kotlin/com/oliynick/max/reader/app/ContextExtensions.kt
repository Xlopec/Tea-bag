package com.oliynick.max.reader.app

import android.content.Context
import android.content.res.Configuration

val Context.isSystemDarkModeEnabled: Boolean
    get() = resources.configuration.isSystemDarkModeEnabled

val Configuration.isSystemDarkModeEnabled: Boolean
    get() = Configuration.UI_MODE_NIGHT_YES == uiMode and Configuration.UI_MODE_NIGHT_MASK