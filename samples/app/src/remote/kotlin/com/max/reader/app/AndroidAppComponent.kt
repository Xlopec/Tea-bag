@file:Suppress("FunctionName")

package com.max.reader.app

import android.app.Application
import com.max.reader.BuildConfig
import com.oliynick.max.reader.app.AppInitializer
import com.oliynick.max.reader.app.DebuggableAppComponent
import com.oliynick.max.reader.app.Environment
import com.oliynick.max.reader.app.command.CloseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

fun AppComponent(
    application: Application,
    scope: CoroutineScope,
    closeCommands: MutableSharedFlow<CloseApp>,
) = Environment(BuildConfig.DEBUG, application, scope, closeCommands::emit)
    .let { env -> DebuggableAppComponent(env, AppInitializer(env)) }