@file:Suppress("FunctionName")

package com.max.reader.app

import android.app.Application
import com.max.reader.BuildConfig
import com.oliynick.max.reader.app.AppComponent
import com.oliynick.max.reader.app.AppInitializer
import com.oliynick.max.reader.app.Environment
import com.oliynick.max.reader.app.command.CloseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

fun AndroidAppComponent(
    application: Application,
    scope: CoroutineScope,
    closeCommands: MutableSharedFlow<CloseApp>,
) = Environment(BuildConfig.DEBUG, application, scope, closeCommands::emit)
    .let { env -> AppComponent(env, AppInitializer(env)) }