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
