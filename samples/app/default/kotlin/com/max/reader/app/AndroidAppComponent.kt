@file:Suppress("FunctionName")

package com.max.reader.app

import android.app.Application
import com.max.reader.BuildConfig
import com.oliynick.max.reader.app.AppComponent
import com.oliynick.max.reader.app.AppInitializer
import com.oliynick.max.reader.app.Environment
import com.oliynick.max.reader.app.isSystemDarkModeEnabled
import kotlinx.coroutines.CoroutineScope

fun AppComponent(
    application: Application,
    scope: CoroutineScope,
) = Environment(BuildConfig.DEBUG, application, scope)
    .let { env -> AppComponent(env, AppInitializer(application.isSystemDarkModeEnabled, env)) }