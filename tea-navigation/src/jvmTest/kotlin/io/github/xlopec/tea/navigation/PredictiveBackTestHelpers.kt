package io.github.xlopec.tea.navigation

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

internal class SpecCounter {
    var count: Int = 0
    fun invoked() { count++ }
}

/**
 * Tracks which entries are currently composed using [DisposableEffect]. Use
 * [Track] inside `PredictiveBackContainer.content` and read [snapshot] after
 * the test reaches a stable state. Reads/writes are single-threaded (test
 * dispatcher) so a plain set is fine.
 */
internal class ComposedEntries {
    private val set = mutableSetOf<TestEntry>()

    fun snapshot(): Set<TestEntry> = set.toSet()

    @Composable
    @Suppress("FunctionName")
    fun Track(entry: TestEntry) {
        DisposableEffect(entry) {
            set += entry
            onDispose { set -= entry }
        }
    }
}

/**
 * A [ScreenTransition] whose placement bumps [counter] every frame it is the active
 * segment's spec. Since the container only evaluates the active spec's placement while a
 * transition is running (`currentState != targetState`), a non-zero count means that
 * mode (push / pop / predictive) drove a transition.
 */
internal fun counting(counter: SpecCounter): ScreenTransition =
    ScreenTransition(tween()) { _, _ -> counter.invoked() }

/** Arbitrary "gesture is partway through" fraction. Exact value isn't load-bearing. */
internal const val MidGestureProgress = 0.5F
