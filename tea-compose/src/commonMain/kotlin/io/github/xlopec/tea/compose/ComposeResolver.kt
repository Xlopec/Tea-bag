package io.github.xlopec.tea.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import io.github.xlopec.compose.nanoTime
import io.github.xlopec.tea.compose.DefaultSnapshotManager.ensureStarted
import io.github.xlopec.tea.compose.DefaultSnapshotManager.started
import io.github.xlopec.tea.core.ExperimentalTeaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.ExperimentalTime
import io.github.xlopec.tea.core.Snapshot as ComponentSnapshot

/**
 * Creates and runs a [ComposeResolver] within the given [scope].
 */
@Suppress("FunctionName")
public fun <M, S, C> ComposeResolver(
    scope: CoroutineScope,
    snapshots: Flow<ComponentSnapshot<M, S, C>>,
    content: @Composable (S, Flow<C>) -> Unit,
) {
    ComposeResolver(scope) {
        val currentSnapshot by snapshots.collectAsState(null)
        val snapshot = currentSnapshot

        if (snapshot != null) {
            val commands = remember(snapshots) { snapshots.flattenCommands() }
            content(snapshot.currentState, commands)
        }
    }
}

/**
 * Creates and runs a [ComposeResolver] within the given [scope] with the provided [content].
 */
@Suppress("FunctionName")
public fun ComposeResolver(scope: CoroutineScope, content: @Composable () -> Unit) {
    val clock = BroadcastFrameClock()
    val recomposer = Recomposer(scope.coroutineContext + clock)
    val composition = Composition(NoOpApplier, recomposer)
    val writes = Channel<Unit>(Channel.CONFLATED)
    val observerHandle = Snapshot.registerGlobalWriteObserver {
        writes.trySend(Unit).getOrThrow()
    }

    scope.launch {
        try {
            recomposer.runRecomposer(clock)
        } finally {
            recomposer.cancel()
            composition.dispose()
            observerHandle.dispose()
            writes.cancel()
        }
    }

    scope.launch {
        writes.consumeEach {
            clock.sendFrame(nanoTime())
        }
    }

    composition.setContent(content)
}

@OptIn(ExperimentalTeaApi::class)
internal fun <C> Flow<ComponentSnapshot<*, *, C>>.flattenCommands() =
    transform { snapshot -> snapshot.commands.forEach { emit(it) } }

@OptIn(ExperimentalTime::class)
private suspend fun Recomposer.runRecomposer(
    internalClock: MonotonicFrameClock,
    snapshotManager: SnapshotManager = DefaultSnapshotManager
) = coroutineScope {

    launch(internalClock, CoroutineStart.UNDISPATCHED) {
        runRecomposeAndApplyChanges()
    }

    launch {
        snapshotManager.ensureStarted()
    }

    awaitIdle()
}

/**
 * Manages registering the callback for global snapshot states.
 */
public interface SnapshotManager {
    /**
     * Registers an observer to global snapshot states and sends an apply notification
     * for each callback if it has not already started.
     */
    public suspend fun ensureStarted()
}

/**
 * Manages registering the callback for the global snapshot states and sending an apply
 * notification for each callback checking if has not started with [started]. You typically
 * call [ensureStarted] before creating a composition. This is a slightly different implementation
 * of androidx.compose.ui.platform.GlobalSnapshotManager.
 */
@OptIn(ExperimentalAtomicApi::class)
internal object DefaultSnapshotManager : SnapshotManager {
    private val started = AtomicBoolean(false)

    /**
     * Registers an observer to global snapshot states and sends an apply notification
     * for each callback if it has not already started.
     */
    override suspend fun ensureStarted() = coroutineScope {
        if (started.compareAndSet(expectedValue = false, newValue = true)) {
            val channel = Channel<Unit>(Channel.Factory.CONFLATED)
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
