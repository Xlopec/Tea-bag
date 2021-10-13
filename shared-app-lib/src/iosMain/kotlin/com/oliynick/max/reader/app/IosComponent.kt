package com.oliynick.max.reader.app

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun interface Cancellation {
    fun cancel()
}

class IosComponent(
    env: PlatformEnv
) {

    private val scope = CoroutineScope(Main + Job())

    private val component = Environment(env)
        .let { env -> AppComponent(env, AppInitializer(env)) }

    private val messages = MutableSharedFlow<Message>()

    fun dispatchNon(
        message: Message
    ) {
        val e = messages.tryEmit(message)
        println("Emit $e")
    }

    fun dispatch(
        message: Message
    ) {
        scope.launch {
            messages.emit(message)
        }
    }

    fun render(
        renderCallback: (AppState) -> Unit
    ): Cancellation {

        val scope = CoroutineScope(Main + Job())

        component(messages)
            .onEach { renderCallback(it) }
            .launchIn(scope)

        return Cancellation { scope.cancel() }
    }

}