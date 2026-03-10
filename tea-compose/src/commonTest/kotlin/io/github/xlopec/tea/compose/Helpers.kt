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
    val exceptions: List<Throwable>
        field = mutableListOf<Throwable>()

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exceptions += exception
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
