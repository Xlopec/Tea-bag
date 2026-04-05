/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

package io.github.xlopec.tea.compose

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.concurrency.synchronize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Scope that allows launching coroutines that are tracked by an identifier.
 */
public interface TrackingScope : CoroutineScope {

    /**
     * Launches a new coroutine and cancels previous launched coroutine with the same [id]
     */
    public fun launchSingle(
        id: Any?,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job
}

/**
 * Creates a [TrackingScope] with the given [context].
 */
public fun TrackingScope(
    context: CoroutineContext
): TrackingScope = TrackingScopeImpl(context)

@OptIn(InternalCoroutinesApi::class)
internal class TrackingScopeImpl(
    context: CoroutineContext
) : TrackingScope {

    override val coroutineContext: CoroutineContext = context + Job(parent = context[Job.Key])
    private val jobsRegistry = ConcurrentMutableMap<Any?, Job>()

    override fun launchSingle(
        id: Any?,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        val job = launch(context = context, block = block, start = CoroutineStart.LAZY)

        jobsRegistry.put(id, job)?.cancel()

        job.invokeOnCompletion {
            jobsRegistry.synchronize {
                if (jobsRegistry[id] === job) {
                    jobsRegistry.remove(id)
                }
            }
        }

        job.start()

        return job
    }
}
