package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.feature.storage.SettingsDelegate
import com.oliynick.max.reader.app.feature.storage.appSettings
import com.oliynick.max.tea.core.component.states
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.oliynick.max.reader.app.Settings as AppSettings

fun interface Cancellation {
    fun cancel()
}

class IosComponent(
    private val systemDarkModeEnabled: Boolean
) {

    private val componentJob = Job()
    private val componentScope = CoroutineScope(Main + componentJob)

    private val environment = Environment(componentScope)
    private val component =
        AppComponent(environment, AppInitializer(systemDarkModeEnabled, environment)).states()

    private val messages = MutableSharedFlow<Message>()

    val appSettings: AppSettings
        get() = SettingsDelegate.appSettings(systemDarkModeEnabled)

    fun dispatch(
        message: Message
    ) {
        componentScope.launch {
            messages.emit(message)
        }
    }

    fun destroy() {
        componentScope.cancel()
    }

    fun render(
        renderCallback: (AppState) -> Unit
    ): Cancellation {

        val renderScope = CoroutineScope(Main + Job(parent = componentJob))

        component(messages)
            .onEach { renderCallback(it) }
            .launchIn(renderScope)

        return Cancellation { renderScope.cancel() }
    }

}