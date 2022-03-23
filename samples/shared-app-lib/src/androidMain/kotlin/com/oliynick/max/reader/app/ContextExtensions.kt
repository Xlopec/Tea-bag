package com.oliynick.max.reader.app

import android.content.Context
import android.content.res.Configuration

val Context.systemDarkModeEnabled: Boolean
    get() = resources.configuration.systemDarkModeEnabled

val Configuration.systemDarkModeEnabled: Boolean
    get() = Configuration.UI_MODE_NIGHT_YES == uiMode and Configuration.UI_MODE_NIGHT_MASK
