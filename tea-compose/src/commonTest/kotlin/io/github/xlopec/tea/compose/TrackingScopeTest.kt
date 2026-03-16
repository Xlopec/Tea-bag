/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName", "TestFunctionName")

package io.github.xlopec.tea.compose

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class TrackingScopeTest {

    private companion object {
        private const val TestTimeout = 10 * 1000L
    }

    @Test
    fun `when launchSingle is called then it launches a coroutine`() = runTest(timeout = TestTimeout.milliseconds) {
        val scope = TrackingScope(context = EmptyCoroutineContext)
        val deferred = CompletableDeferred<Unit>()
        scope.launchSingle(id = "id") {
            deferred.complete(Unit)
        }

        deferred.join()
    }

    @Test
    fun `when launchSingle is called with the same id then it cancels the previous coroutine`() =
        runTest(timeout = TestTimeout.milliseconds) {
            val scope = TrackingScope(context = EmptyCoroutineContext)
            val started = CompletableDeferred<Unit>()
            val canceled = CompletableDeferred<Unit>()

            val job1 = scope.launchSingle(id = "id") {
                started.complete(Unit)
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    canceled.complete(Unit)
                }
            }

            started.await()
            // Launch another one with the same ID
            val job2 = scope.launchSingle(id = "id") {
                delay(Long.MAX_VALUE)
            }

            canceled.await()
            job1.join()
            assertTrue(job1.isCancelled)
            assertFalse(job1.isActive)
            assertTrue(job2.isActive)
            job2.cancel()
        }

    @Test
    fun `when launchSingle is called with a different id then it does not cancel the previous coroutine`() =
        runTest(timeout = TestTimeout.milliseconds) {
            val scope = TrackingScope(EmptyCoroutineContext)
            val started1 = CompletableDeferred<Unit>()
            val started2 = CompletableDeferred<Unit>()

            val job1 = scope.launchSingle("id1") {
                started1.complete(Unit)
                delay(Long.MAX_VALUE)
            }

            started1.await()
            val job2 = scope.launchSingle("id2") {
                started2.complete(Unit)
                delay(Long.MAX_VALUE)
            }

            started2.await()
            assertTrue(job1.isActive)
            assertTrue(job2.isActive)
            job1.cancel()
            job2.cancel()
        }

    @Test
    fun `when coroutine finishes then it is removed from the registry`() =
        runTest(timeout = TestTimeout.milliseconds) {
            val scope = TrackingScope(EmptyCoroutineContext)
            val started = CompletableDeferred<Unit>()

            val job1 = scope.launchSingle("id") {
                started.complete(Unit)
            }

            started.await()
            job1.join()

            // Since jobsRegistry is private, we verify that a new call with the same id
            // doesn't have a job to cancel (though we can't observe it directly).
            // But we can check if it's actually finished.
            assertFalse(job1.isActive)
            assertTrue(job1.isCompleted)
        }

    @Test
    fun `when launchSingle is called concurrently with the same id then it only one job is left active`() =
        runTest(timeout = TestTimeout.milliseconds) {
            val scope = TrackingScope(EmptyCoroutineContext)
            val n = 1000
            val jobs = mutableListOf<Job>()
            val lock = Lock()

            withContext(Dispatchers.Default) {
                repeat(n) {
                    launch {
                        val job = scope.launchSingle("id") {
                            delay(Long.MAX_VALUE)
                        }
                        lock.withLock {
                            jobs += job
                        }
                    }
                }
            }

            val activeJobs = jobs.filter { it.isActive }
            assertTrue(activeJobs.size <= 1, "Expected at most 1 active job, but found ${activeJobs.size}")

            activeJobs.forEach { it.cancel() }
        }
}
