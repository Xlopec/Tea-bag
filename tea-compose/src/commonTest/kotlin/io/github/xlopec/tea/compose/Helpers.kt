package io.github.xlopec.tea.compose

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

internal class RecordingExceptionHandler : CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler.Key
    val exceptions: List<Throwable>
        field = mutableListOf<Throwable>()

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exceptions += exception
    }
}

internal enum class DisposableEffectState { NOT_LAUNCHED, LAUNCHED, DISPOSED }
