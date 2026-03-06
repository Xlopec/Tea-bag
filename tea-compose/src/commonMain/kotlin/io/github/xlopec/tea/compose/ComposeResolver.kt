package io.github.xlopec.tea.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import io.github.xlopec.compose.nanoTime
import io.github.xlopec.tea.compose.DefaultSnapshotManager.ensureStarted
import io.github.xlopec.tea.compose.SnapshotNotifierPolicy.External
import io.github.xlopec.tea.compose.SnapshotNotifierPolicy.WhileActive
import io.github.xlopec.tea.core.ExperimentalTeaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import io.github.xlopec.tea.core.Snapshot as ComponentSnapshot

/**
 * Creates and runs a [ComposeResolver] within the given [scope].
 */
@Suppress("FunctionName")
public fun <M, S, C> ComposeResolver(
    scope: CoroutineScope,
    snapshots: Flow<ComponentSnapshot<M, S, C>>,
    snapshotManagerPolicy: SnapshotNotifierPolicy = External,
    content: @Composable (S, Flow<C>) -> Unit,
) {
    ComposeResolver(scope, snapshotManagerPolicy) {
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
public fun ComposeResolver(
    scope: CoroutineScope,
    snapshotManagerPolicy: SnapshotNotifierPolicy = External,
    content: @Composable () -> Unit
) {
    val clock = BroadcastFrameClock()
    val recomposer = Recomposer(scope.coroutineContext + clock)
    val composition = Composition(NoOpApplier, recomposer)
    val writes = Channel<Unit>(Channel.CONFLATED)
    val observerHandle = Snapshot.registerGlobalWriteObserver {
        writes.trySend(Unit).getOrThrow()
    }

    scope.launch {
        try {
            recomposer.runRecomposer(clock, snapshotManagerPolicy)
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
    snapshotNotifierPolicy: SnapshotNotifierPolicy
) = coroutineScope {

    launch(internalClock, CoroutineStart.UNDISPATCHED) {
        runRecomposeAndApplyChanges()
    }

    when (snapshotNotifierPolicy) {
        External -> Unit
        WhileActive -> launch {
            ensureStarted()
        }
    }

    awaitIdle()
}
