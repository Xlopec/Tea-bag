package io.github.xlopec.tea.compose

import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Manages registering the callback for the global snapshot states and sending an apply
 * notification for each callback checking if has not started with [started]. You typically
 * call [ensureStarted] before creating a composition. This is a slightly different implementation
 * of androidx.compose.ui.platform.GlobalSnapshotManager.
 */
@OptIn(ExperimentalAtomicApi::class)
internal object DefaultSnapshotManager {
    private val started = AtomicBoolean(false)

    /**
     * Registers an observer to global snapshot states and sends an apply notification
     * for each callback if it has not already started.
     */
    suspend fun ensureStarted() = coroutineScope {
        if (started.compareAndSet(expectedValue = false, newValue = true)) {
            val channel = Channel<Unit>(Channel.CONFLATED)
            launch {
                channel.consumeEach {
                    Snapshot.sendApplyNotifications()
                }
            }
            var handle: ObserverHandle? = null
            try {
                handle = Snapshot.registerGlobalWriteObserver {
                    channel.trySend(Unit)
                }
                awaitCancellation()
            } finally {
                handle?.dispose()
            }
        }
    }
}
