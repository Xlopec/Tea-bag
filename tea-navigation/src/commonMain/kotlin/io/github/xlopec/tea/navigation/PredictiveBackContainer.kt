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

package io.github.xlopec.tea.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Renders [stack]'s top entry and animates between entries on push / pop, with built-in
 * predictive-back gesture support driven by [PredictiveBackGestureOverlay].
 *
 * The three transition spec parameters default to iOS-style slides
 * ([PushTransitionSpec], [PopTransitionSpec], [PredictivePopTransitionSpec]):
 * a parallax slide for push / programmatic pop, and the same slide with linear
 * easing for the predictive back gesture (linear easing keeps the screen pinned
 * to the finger during the seek; non-linear would make it run ahead). Pass your
 * own [ContentTransform] factory to any of the three parameters to switch feel.
 *
 * @param stack current navigation stack; the top entry is rendered
 * @param previousScreenFor returns the entry to reveal during a back gesture from `current`,
 * or `null` if back should be ignored for the current entry
 * @param onBackComplete invoked after a back gesture is confirmed, with the entry that was popped
 * @param transitionSpec [ContentTransform] factory used for forward navigation
 * @param popTransitionSpec [ContentTransform] factory used for programmatic pops
 * @param predictivePopTransitionSpec [ContentTransform] factory used while the back gesture is in flight
 * and to finish/cancel after release
 * @param modifier modifier applied to the container
 * @param startEdgeEnabled enables the start-edge swipe (left in LTR, right in RTL)
 * @param endEdgeEnabled enables the end-edge swipe (right in LTR, left in RTL)
 * @param edgeWidth horizontal distance from each enabled edge where the gesture activates
 * @param activationOffsetThreshold drag distance from the initial touch required to start the gesture
 * @param confirmationProgressThreshold progress fraction at release that confirms the back
 * @param content per-entry composable
 */
@Composable
public fun <T : NavStackEntry<*>> PredictiveBackContainer(
    stack: NavigationStack<T>,
    previousScreenFor: (stack: NavigationStack<T>, current: T) -> T?,
    onBackComplete: (T) -> Unit,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PushTransitionSpec(),
    popTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PopTransitionSpec(),
    predictivePopTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PredictivePopTransitionSpec(),
    startEdgeEnabled: Boolean = true,
    endEdgeEnabled: Boolean = true,
    edgeWidth: Dp = 16.dp,
    activationOffsetThreshold: Dp = 16.dp,
    confirmationProgressThreshold: Float = 0.2F,
    content: @Composable (T) -> Unit,
) {
    var current by remember { mutableStateOf(stack.screen) }
    var previous by remember { mutableStateOf<T?>(null) }
    var progress by remember { mutableFloatStateOf(0F) }

    val canHandleBack = previousScreenFor(stack, current) != null

    PredictiveBackGestureOverlay(
        modifier = modifier,
        enabled = canHandleBack,
        startEdgeEnabled = startEdgeEnabled,
        endEdgeEnabled = endEdgeEnabled,
        edgeWidth = edgeWidth,
        activationOffsetThreshold = activationOffsetThreshold,
        confirmationProgressThreshold = confirmationProgressThreshold,
        onStart = {
            val prev = previousScreenFor(stack, current) ?: return@PredictiveBackGestureOverlay
            previous = prev
            progress = 0F
        },
        onProgress = { progress = it },
        onConfirm = {
            val popped = current
            val prev = previous ?: return@PredictiveBackGestureOverlay
            Snapshot.withMutableSnapshot {
                // Atomic swap: setting current=prev BEFORE clearing previous so the
                // recomposition triggered by `previous = null` sees the new `current`.
                // This also lets the post-gesture branch's `targetState == current`
                // check resolve "completed" instead of "cancelled".
                current = prev
                previous = null
            }
            onBackComplete(popped)
        },
        onCancel = {
            // Leave `current` untouched so the post-gesture branch sees
            // `targetState != current` and animates the lifted fraction back to 0.
            previous = null
        },
    ) {
        val transitionState = remember { SeekableTransitionState(current) }
        val transition = rememberTransition(transitionState, label = "Screen transition")

        // Stack snapshot at the moment the transition's currentState last settled.
        // Used by `isPop` to detect post-gesture / programmatic pops vs pushes.
        // The stale capture here is intentional — it's the deliberate
        // counterpart to `canHandleBack`'s fresh read above (same `remember(...) { stack }`
        // shape, opposite intent): there we want every stack change observed;
        // here we want a frozen baseline that only refreshes when the
        // transition settles.
        val transitionCurrentStackSnapshot = remember(transition.currentState) { stack }
        val isPop = isPop(transitionCurrentStackSnapshot, stack)

        // Sync `current` to external stack mutations. If a gesture is in
        // flight we cancel it: clear the gesture state, then snap the seekable
        // transition back to rest so the post-gesture branch below drives a
        // clean push/pop animation from the new top.
        LaunchedEffect(stack) {
            if (previous != null) {
                Snapshot.withMutableSnapshot {
                    previous = null
                    progress = 0F
                }
                transitionState.snapTo(current)
            }
            current = stack.screen
        }

        val prev = previous

        val contentTransform: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
            when {
                prev != null -> predictivePopTransitionSpec(this)
                isPop -> popTransitionSpec(this)
                else -> transitionSpec(this)
            }
        }

        if (prev != null) {
            // snapshotFlow conflates progress updates so heavy content
            // (e.g. WebView) doesn't drop frames from per-emission
            // LaunchedEffect cancel/restart.
            LaunchedEffect(prev) {
                snapshotFlow { progress }.collect { transitionState.seekTo(it, prev) }
            }
        } else {
            LaunchedEffect(current) {
                if (transitionState.currentState != current) {
                    // Regular forward push or programmatic pop — no lifted fraction.
                    transitionState.animateTo(current)
                    return@LaunchedEffect
                }
                // currentState == current. Two sub-cases:
                //   - targetState == current: already-settled (initial composition,
                //     or branch was re-added without any in-flight seek). Skip
                //   - targetState != current: predictive back cancel. Rewind the
                //     lifted fraction back to 0 while keeping currentState/targetState
                //     intact — `animateTo(currentState)` would visibly glitch here
                //     (it snaps fraction=0 and swaps currentState↔targetState before
                //     animating, see SeekableTransitionState.animateTo source).
                if (transitionState.targetState == current) return@LaunchedEffect
                val totalDuration = (transition.totalDurationNanos / 1_000_000).toInt()
                val remaining = (transitionState.fraction * totalDuration).toInt().coerceAtLeast(1)
                animate(
                    initialValue = transitionState.fraction,
                    targetValue = 0F,
                    animationSpec = tween(remaining),
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        if (value != 0F) transitionState.seekTo(value)
                        else transitionState.snapTo(current)
                    }
                }
            }
        }

        transition.AnimatedContent(
            transitionSpec = contentTransform,
            contentKey = { it.id },
        ) { animatedScreen ->
            content(animatedScreen)
        }
    }
}

/**
 * Detects whether [new] is a clean subset of [old] (i.e. a pop). Compares by
 * [NavStackEntry.id] so the check works regardless of whether entries are data classes.
 *
 * Adapted from `androidx.navigation3.ui.NavDisplay.isPop`.
 */
private fun <T : NavStackEntry<*>> isPop(old: NavigationStack<T>, new: NavigationStack<T>): Boolean {
    if (old.firstOrNull()?.id != new.firstOrNull()?.id) return false
    if (new.size > old.size) return false
    val diverging = new.indices.firstOrNull { i -> new[i].id != old[i].id }
    return diverging == null && new.size != old.size
}
