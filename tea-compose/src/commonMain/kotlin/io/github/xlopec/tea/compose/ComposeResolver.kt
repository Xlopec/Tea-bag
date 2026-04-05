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

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
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
