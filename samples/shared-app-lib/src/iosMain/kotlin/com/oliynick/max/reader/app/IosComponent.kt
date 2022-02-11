@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.component.states
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun interface Cancellation {
    fun cancel()
}

class IosComponent(
    systemDarkModeEnabled: Boolean
) {
    private val componentJob = Job()
    private val componentScope = CoroutineScope(Main + componentJob)
    private val component = AppComponent(systemDarkModeEnabled, componentScope).states()
    private val messages = MutableSharedFlow<Message>()

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

private fun AppComponent(
    systemDarkModeEnabled: Boolean,
    coroutineScope: CoroutineScope
) = Environment(coroutineScope)
    .let { AppComponent(it, AppInitializer(systemDarkModeEnabled, it)) }