@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import android.app.Application
import io.github.xlopec.reader.BuildConfig
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.tea.core.Component
import kotlinx.coroutines.CoroutineScope

fun AppComponent(
    application: Application,
    scope: CoroutineScope,
): Component<Message, AppState, Command> = Environment(BuildConfig.DEBUG, application, scope)
    .let { env -> AppComponent(env, AppInitializer(application.systemDarkModeEnabled, env)) }
