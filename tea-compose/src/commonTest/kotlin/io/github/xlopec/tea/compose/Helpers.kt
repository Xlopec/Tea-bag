package io.github.xlopec.tea.compose

import androidx.compose.runtime.BroadcastFrameClock
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class RecordingExceptionHandler : CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler.Key
    private val _exceptions = mutableListOf<Throwable>()
    val exceptions: List<Throwable>
        get() = _exceptions

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        _exceptions += exception
    }
}

internal enum class DisposableEffectState { NOT_LAUNCHED, LAUNCHED, DISPOSED }

internal fun runTestWith(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestScope.(job: Job, clock: BroadcastFrameClock, scope: CoroutineScope) -> Unit,
) = runTest {
    val job = Job()
    val clock = BroadcastFrameClock()
    val scope = CoroutineScope(coroutineContext + job + clock + context)

    try {
        testBody(job, clock, scope)
    } finally {
        job.cancelAndJoin()
    }
}
