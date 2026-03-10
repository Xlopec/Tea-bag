package io.github.xlopec.tea.compose

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class)
class ComposeResolverTest {

    @Test
    fun `when composing items then correct sequence of values is produced`() = runTestWith { _, clock, scope ->
        val value = AtomicInt(0)

        ComposeResolver(
            scope = scope,
            clockPolicy = ClockPolicy.External,
            snapshotManagerPolicy = SnapshotNotifierPolicy.WhileActive,
        ) {
            var count by remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) {
                while (isActive) {
                    value.store(count)
                    delay(100)
                    count++
                }
            }
        }

        assertEquals(0, value.load())

        clock.sendFrame(0)
        assertEquals(0, value.load())

        advanceTimeBy(99)
        runCurrent()
        clock.sendFrame(0)
        assertEquals(0, value.load())

        advanceTimeBy(1)
        runCurrent()
        clock.sendFrame(0)
        assertEquals(1, value.load())

        advanceTimeBy(100)
        runCurrent()
        clock.sendFrame(0)
        assertEquals(2, value.load())
    }

    @Test
    fun `when compose block throws then exception is propagated immediately`() = runTestWith { _, _, scope ->
        val runtimeException = RuntimeException()
        val exception = runCatching {
            ComposeResolver(scope) {
                throw runtimeException
            }
        }.exceptionOrNull()

        assertEquals(runtimeException, exception)
    }

    @Test
    fun `when effect throws then exception is caught by handler and scope is canceled`() =
        runTestWith(RecordingExceptionHandler()) { job, clock, scope ->
            val exceptionHandler = scope.coroutineContext[CoroutineExceptionHandler.Key] as RecordingExceptionHandler

            val runtimeException = object : RuntimeException() {}
            ComposeResolver(scope) {
                LaunchedEffect(Unit) {
                    delay(50)
                    throw runtimeException
                }
            }

            advanceTimeBy(50)
            runCurrent()
            clock.sendFrame(0)
            runCurrent()
            assertEquals(runtimeException, exceptionHandler.exceptions.single())

            assertTrue(job.isCompleted)
        }

    @Test
    fun `when scope is canceled then disposable effect is disposed`() = runTestWith { job, _, scope ->
        var state: DisposableEffectState = DisposableEffectState.NOT_LAUNCHED

        ComposeResolver(
            scope = scope,
            clockPolicy = ClockPolicy.External,
            snapshotManagerPolicy = SnapshotNotifierPolicy.WhileActive,
        ) {
            DisposableEffect(Unit) {
                state = DisposableEffectState.LAUNCHED

                onDispose {
                    state = DisposableEffectState.DISPOSED
                }
            }
        }

        assertEquals(DisposableEffectState.LAUNCHED, state)

        job.cancelAndJoin()
        runCurrent()
        assertEquals(DisposableEffectState.DISPOSED, state)
    }

    @Test
    fun `when composing then provided coroutine context is used`() =
        runTestWith(CoroutineName("test_key")) { _, _, scope ->
            val expectedName = CoroutineName("test_key")

            var actualName: CoroutineName? = null
            ComposeResolver(scope) {
                actualName = rememberCoroutineScope().coroutineContext[CoroutineName]
            }
            assertEquals(expectedName, actualName)
        }

    @Test
    fun `when snapshot notifier policy is WhileActive then composition is automatically notified of state mutation`() =
        runTestWith { _, clock, scope ->
            val value = AtomicInt(-1)

            var count by mutableIntStateOf(0)

            ComposeResolver(
                scope = scope,
                clockPolicy = ClockPolicy.External,
                snapshotManagerPolicy = SnapshotNotifierPolicy.WhileActive
            ) {
                value.store(count)
            }

            assertEquals(0, value.load())

            // The composition is automatically notified of the state mutation.
            count++
            runCurrent()
            clock.sendFrame(0)
            assertEquals(1, value.load())
        }

    @Test
    fun `when snapshot notifier policy is External then composition isn't automatically notified of state mutation`() =
        runTestWith { _, clock, scope ->
            val value = AtomicInt(-1)

            var count by mutableIntStateOf(0)

            ComposeResolver(
                scope = scope,
                clockPolicy = ClockPolicy.External,
                snapshotManagerPolicy = SnapshotNotifierPolicy.External
            ) {
                value.store(count)
            }

            assertEquals(0, value.load())

            // The composition isn't automatically notified of the state mutation.
            count++
            runCurrent()
            clock.sendFrame(0)
            assertEquals(0, value.load())

            // The composition notified of state mutation when sendApplyNotifications is called.
            Snapshot.sendApplyNotifications()
            runCurrent()
            clock.sendFrame(0)
            assertEquals(1, value.load())
        }
}
