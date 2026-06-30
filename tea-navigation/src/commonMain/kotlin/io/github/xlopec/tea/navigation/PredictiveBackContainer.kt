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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Renders [stack]'s top entry and animates between entries on push / pop, with built-in
 * predictive-back gesture support driven by the system back dispatcher (Android
 * predictive back, iOS start-edge swipe) via
 * [androidx.navigationevent.compose.NavigationBackHandler].
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
 */
@Composable
public fun <T : NavStackEntry<*>> PredictiveBackContainer(
    stack: NavigationStack<T>,
    previousScreenFor: (stack: NavigationStack<T>, current: T) -> T?,
    onBackComplete: (T) -> Unit,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PushTransitionSpec(),
    popTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PopTransitionSpec(),
    predictivePopTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform =
        PredictivePopTransitionSpec(),
    content: @Composable (T) -> Unit,
) {
    var current by remember { mutableStateOf(stack.screen) }
    var previous by remember { mutableStateOf<T?>(null) }
    var progress by remember { mutableStateOf(0F) }
    val transitionState = remember { SeekableTransitionState(current) }

    val canHandleBack = previousScreenFor(stack, current) != null
    val navState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navState,
        isBackEnabled = canHandleBack,
        onBackCancelled = {
            // No-op here. The container can't tell cancel from complete just from
            // its own handler callback (it may not even be the active handler
            // — a screen-level handler wins LIFO), so the gesture-end resolution
            // is driven from the dispatcher's transitionState collector below.
        },
        onBackCompleted = {
            val popped = current
            val prev = previous ?: previousScreenFor(stack, current) ?: return@NavigationBackHandler
            Snapshot.withMutableSnapshot {
                current = prev
                previous = null
            }
            onBackComplete(popped)
        },
    )

    val owner = LocalNavigationEventDispatcherOwner.current
    // Hoist live stack / resolver into snapshot state so the long-lived
    // gesture collector (LaunchedEffect(Unit)) reads the latest value rather
    // than the closure capture from initial composition — without this, the
    // collector would resolve `previous` against a stale stack and fail to set it.
    val stackState = rememberUpdatedState(stack)
    val previousResolverState = rememberUpdatedState(previousScreenFor)
    // Tracks whether a gesture was actually in flight, so the Idle → cancel
    // animation only kicks in for a real gesture end, not for the initial
    // Idle replay that StateFlow emits on first collection.
    var wasInProgress by remember { mutableStateOf(false) }
    // Job for the cancel-resolution animation. We start it speculatively when
    // the dispatcher returns to Idle; if a stack mutation races in (the
    // gesture was a complete), the stack LaunchedEffect cancels it and runs
    // the complete-animation instead.
    val cancelJobRef = remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(Unit) {
        // Read from `dispatcher.transitionState` (the global StateFlow) rather
        // than `navState.transitionState` because the latter only updates on the
        // active topmost handler — when a screen-level handler (e.g. ArticleDetails)
        // is composed inside this container, it wins LIFO and our local state
        // stays Idle. The dispatcher's flow always reflects the active gesture.
        // On platforms without in-flight progress (Android <14, desktop, web)
        // the dispatcher fires onBackCompleted atomically without ever entering
        // InProgress — the InProgress branch never fires and the stack-mutation
        // path animates the pop normally.
        val flow = owner?.navigationEventDispatcher?.transitionState ?: return@LaunchedEffect
        flow.collect { state ->
            when (state) {
                is NavigationEventTransitionState.InProgress -> {
                    cancelJobRef.value?.cancel()
                    cancelJobRef.value = null
                    wasInProgress = true
                    if (previous == null) {
                        previous = previousResolverState.value(stackState.value, current)
                            ?: return@collect
                    }
                    progress = state.latestEvent.progress
                }
                NavigationEventTransitionState.Idle -> {
                    if (wasInProgress && previous != null) {
                        wasInProgress = false
                        // Speculative cancel. If the gesture was actually a
                        // complete, the topmost handler (e.g. ArticleDetails)
                        // mutates the stack right after this, and the stack
                        // LaunchedEffect cancels this job before it finishes.
                        cancelJobRef.value = launch {
                            val start = progress
                            animate(
                                initialValue = start,
                                targetValue = 0F,
                                animationSpec = tween((start * CancelDurationMs).toInt().coerceAtLeast(MinDurationMs)),
                            ) { value, _ -> progress = value }
                            previous = null
                            cancelJobRef.value = null
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        val transition = rememberTransition(transitionState, label = "Screen transition")

        // Stack snapshot at the moment the transition's currentState last settled.
        // Used by `isPop` to detect post-gesture / programmatic pops vs pushes.
        // The stale capture is intentional — frozen baseline that only refreshes
        // when the transition settles.
        val transitionCurrentStackSnapshot = remember(transition.currentState) { stack }
        val isPop = isPop(transitionCurrentStackSnapshot, stack)

        // Sync `current` to external stack mutations. Three cases:
        // 1. The mutation matches our in-flight gesture (new top == `previous`):
        //    smoothly drive the seek from current fraction to 1, then settle
        //    on the new top without rebounding.
        // 2. The mutation is unrelated (no in-flight gesture, or the new top
        //    isn't what we were sliding to): snap-cancel and let the
        //    post-gesture branch below animate the push/pop normally.
        // 3. No in-flight gesture: just update `current`.
        LaunchedEffect(stack) {
            val newTop = stack.screen
            val prev = previous
            when {
                prev != null && prev == newTop -> {
                    // Cancel the speculative Idle-cancel animation if it
                    // started; we're committing instead.
                    cancelJobRef.value?.cancel()
                    cancelJobRef.value = null
                    val startFraction = progress
                    if (startFraction < 1F) {
                        animate(
                            initialValue = startFraction,
                            targetValue = 1F,
                            animationSpec = tween(((1F - startFraction) * CompleteDurationMs).toInt().coerceAtLeast(MinDurationMs)),
                        ) { value, _ -> progress = value }
                    }
                    // Seek already at fraction=1 targeting `prev`; settle by
                    // collapsing transitionState onto the new top. snapTo is
                    // visually invisible here because the content shown
                    // (`prev`) equals the new top.
                    Snapshot.withMutableSnapshot {
                        previous = null
                        progress = 0F
                    }
                    transitionState.snapTo(newTop)
                    current = newTop
                }
                prev != null -> {
                    cancelJobRef.value?.cancel()
                    cancelJobRef.value = null
                    Snapshot.withMutableSnapshot {
                        previous = null
                        progress = 0F
                    }
                    transitionState.snapTo(current)
                    current = newTop
                }
                else -> {
                    current = newTop
                }
            }
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
                if (transitionState.targetState == current) return@LaunchedEffect
                // Settle any leftover targetState/fraction mismatch (e.g. after
                // a cancel-animation ran progress to 0 but transitionState still
                // has the gesture target). Snap is fine here because the visible
                // content already matches `current` (fraction is at 0).
                transitionState.snapTo(current)
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

private const val CancelDurationMs = 300
private const val CompleteDurationMs = 300
private const val MinDurationMs = 50

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
