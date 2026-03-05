package io.github.xlopec.reader.app

import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public interface JobRegistry {

    public fun CoroutineScope.launchUnique(
        id: Any?,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job

    public fun CoroutineScope.launchUniqueForScreen(
        id: Any?,
        screen: ScreenId,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job

    public fun CoroutineScope.cancelForScreen(
        id: Any?,
        screen: ScreenId,
    ): Job

    public fun CoroutineScope.cancelScreen(
        screen: ScreenId,
    ): Job

    public fun CoroutineScope.cancel(
        id: Any?,
    ): Job
}

internal fun JobRegistry(): JobRegistry = JobRegistryImpl()

private class JobRegistryImpl : JobRegistry {
    private val globalJobsRegistry: MutableMap<Any?, Job> = mutableMapOf()
    private val screenJobsRegistry: MutableMap<ScreenId, MutableList<JobWithId>> = mutableMapOf()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val updateRegistryContext = Dispatchers.Default.limitedParallelism(1)

    private companion object {
        const val ScreenRegistrySize = 5
    }

    override fun CoroutineScope.launchUnique(
        id: Any?,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch(updateRegistryContext) {
        globalJobsRegistry[id]?.cancel()

        val job = launch(context = context, block = block, start = CoroutineStart.LAZY)
        val jobWithId = JobWithId(id, job)

        globalJobsRegistry[id] = jobWithId

        try {
            job.join()
        } finally {
            globalJobsRegistry.values.removeAll { it === job }
        }
    }

    override fun CoroutineScope.launchUniqueForScreen(
        id: Any?,
        screen: ScreenId,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): Job = launch(updateRegistryContext) {
        // As performance improvement we can synchronize on screen's job registry only, so allowing
        // multiple coroutines access screenJobsRegistry simultaneously
        val screenJobs = screenJobsRegistry.getOrPut(screen) { ArrayList(ScreenRegistrySize) }
            .also { it.removeJob(id) }

        val job = launch(context = context, block = block, start = CoroutineStart.LAZY)
        val jobWithId = JobWithId(id, job)

        screenJobs.add(jobWithId)

        try {
            job.join()
        } finally {
            screenJobsRegistry[screen]?.removeAll { it === job }
        }
    }

    override fun CoroutineScope.cancelForScreen(
        id: Any?,
        screen: ScreenId
    ): Job = launch(updateRegistryContext) {
        screenJobsRegistry[screen]?.removeJob(id)
    }

    override fun CoroutineScope.cancelScreen(
        screen: ScreenId
    ): Job = launch(updateRegistryContext) {
        val jobs = screenJobsRegistry.remove(screen) ?: return@launch

        jobs.fastForEach { identifiable -> identifiable.job.cancel() }
    }

    override fun CoroutineScope.cancel(
        id: Any?
    ): Job = launch(updateRegistryContext) {
        globalJobsRegistry.remove(id)?.cancel()
    }
}

private data class JobWithId(val id: Any?, val job: Job) : Job by job

private fun MutableList<JobWithId>.removeJob(
    id: Any?,
) {
    val index = indexOfFirst { it.id == id }

    if (index >= 0) {
        // cancel and remove
        removeAt(index).job.cancel()
    }
}
