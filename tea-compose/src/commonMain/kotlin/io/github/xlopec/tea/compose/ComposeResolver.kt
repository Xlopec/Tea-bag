package io.github.xlopec.tea.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.ExperimentalTime
import io.github.xlopec.tea.core.Snapshot as ComponentSnapshot

/**
 * Resolves and composes the given snapshot flow in the provided coroutine scope
 * while reacting to component state and commands.
 *
 * ***NOTE*** some snapshots might be skipped if published too frequently, see [androidx.compose.runtime.produceState] for more info
 *
 * @param M The type representing messages in the composition.
 * @param S The type representing state in the composition.
 * @param C The type representing commands in the composition.
 * @param scope The [CoroutineScope] in which the [ComposeResolver] is executed.
 * @param snapshots A [Flow] emitting [ComponentSnapshot] instances, representing
 *                  the state and commands of a component.
 * @param snapshotManagerPolicy The policy determining how snapshot change notifications
 *                               are handled during composition. Defaults to [External].
 * @param content A composable function receiving the current state [S] and a [Flow] of
 *                commands [C] to process and render UI.
 */
@Deprecated("remove")
@Suppress("FunctionName")
public fun <M, S, C> ComposeResolver(
    scope: CoroutineScope,
    snapshots: Flow<ComponentSnapshot<M, S, C>>,
    clockPolicy: ClockPolicy = ClockPolicy.External,
    snapshotManagerPolicy: SnapshotNotifierPolicy = External,
    content: @Composable (S, Flow<C>) -> Unit,
) {
    ComposeResolver(scope, clockPolicy, snapshotManagerPolicy) {
        val currentSnapshot by snapshots.collectAsState(null)
        val snapshot = currentSnapshot

        if (snapshot != null) {
            val commands = remember(snapshots) { snapshots.flattenCommands() }
            content(snapshot.currentState, commands)
        }
    }
}

/**
 * Resolves and composes the given snapshot flow in the provided coroutine scope
 *
 * @param scope The [CoroutineScope] in which the [ComposeResolver] is executed.
 * @param snapshotManagerPolicy The policy determining how snapshot change notifications
 *                               are handled during composition. Defaults to [External].
 * @param content composable content.
 */
@Suppress("FunctionName")
public fun ComposeResolver(
    scope: CoroutineScope,
    clockPolicy: ClockPolicy = ClockPolicy.External,
    snapshotManagerPolicy: SnapshotNotifierPolicy = External,
    content: @Composable () -> Unit
) {
    val clockContext = when (clockPolicy) {
        ClockPolicy.External -> EmptyCoroutineContext
        ClockPolicy.Internal -> BroadcastFrameClock().also { clock ->
            val writes = Channel<Unit>(Channel.CONFLATED)
            val observerHandle = Snapshot.registerGlobalWriteObserver {
                writes.trySend(Unit).getOrThrow()
            }

            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    writes.consumeEach {
                        clock.sendFrame(nanoTime())
                    }
                } finally {
                    observerHandle.dispose()
                    writes.cancel()
                }
            }
        }
    }
    // todo fix wrong contenxt - should be only one clock
    val finalContext = scope.coroutineContext + clockContext
    val recomposer = Recomposer(finalContext)
    val composition = Composition(NoOpApplier, recomposer)

    scope.launch(finalContext, start = CoroutineStart.UNDISPATCHED) {
        try {
            when (snapshotManagerPolicy) {
                External -> Unit
                WhileActive -> launch(start = CoroutineStart.UNDISPATCHED) {
                    DefaultSnapshotManager.ensureStarted()
                }
            }

            recomposer.runRecomposeAndApplyChanges()
        } finally {
            recomposer.cancel()
            composition.dispose()
        }
    }

    composition.setContent(content)
}

@OptIn(ExperimentalTime::class)
private suspend fun Recomposer.runRecomposer(
    frameClockContext: CoroutineContext,
    snapshotNotifierPolicy: SnapshotNotifierPolicy
) = coroutineScope {
    when (snapshotNotifierPolicy) {
        External -> Unit
        WhileActive -> launch {
            DefaultSnapshotManager.ensureStarted()
        }
    }

    launch(frameClockContext, CoroutineStart.UNDISPATCHED) {
        runRecomposeAndApplyChanges()
    }
}

@OptIn(ExperimentalTeaApi::class)
private fun <C> Flow<ComponentSnapshot<*, *, C>>.flattenCommands() =
    transform { snapshot -> snapshot.commands.forEach { emit(it) } }
