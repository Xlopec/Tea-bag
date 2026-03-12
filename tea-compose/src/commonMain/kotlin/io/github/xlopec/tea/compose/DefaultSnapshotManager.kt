package io.github.xlopec.tea.compose

import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import io.github.xlopec.tea.compose.DefaultSnapshotManager.ensureStarted
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Manages registering an observer for global snapshot changes and sending apply notifications
 * when state changes are detected.
 *
 * This class is an alternative implementation of `androidx.compose.ui.platform.GlobalSnapshotManager`
 * intended for use in environments where the standard UI-based snapshot manager is not available.
 * Typically, [ensureStarted] is called before creating a composition.
 */
@OptIn(ExperimentalAtomicApi::class)
internal object DefaultSnapshotManager {

    private val started = AtomicBoolean(false)

    /**
     * Starts the snapshot management loop.
     *
     * This function registers a global write observer and calls [Snapshot.sendApplyNotifications]
     * whenever a write occurs. It is a long-running suspending function that remains active
     * until the calling coroutine scope is cancelled.
     *
     * Multiple concurrent calls to this function are safe; only the first one will start the
     * management loop, subsequent calls will return immediately if it's already started.
     * However, the loop is tied to the scope of the **first** successful caller.
     */
    suspend fun ensureStarted() = coroutineScope {
        if (started.compareAndSet(expectedValue = false, newValue = true)) {
            var handle: ObserverHandle? = null
            try {
                val channel = Channel<Unit>(Channel.CONFLATED)
                launch {
                    channel.consumeEach {
                        Snapshot.sendApplyNotifications()
                    }
                }
                handle = Snapshot.registerGlobalWriteObserver {
                    channel.trySend(Unit).getOrThrow()
                }
                awaitCancellation()
            } finally {
                started.compareAndSet(expectedValue = true, newValue = false)
                handle?.dispose()
            }
        }
    }
}
