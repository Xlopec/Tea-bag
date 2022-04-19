@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import android.app.Application
import io.github.xlopec.reader.BuildConfig
import kotlinx.coroutines.CoroutineScope

fun AppComponent(
    application: Application,
    scope: CoroutineScope,
) = Environment(BuildConfig.DEBUG, application, scope)
    .let { env -> AppComponent(env, AppInitializer(application.systemDarkModeEnabled, env)) }