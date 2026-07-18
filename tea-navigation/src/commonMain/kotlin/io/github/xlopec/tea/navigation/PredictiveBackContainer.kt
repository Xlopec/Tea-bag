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
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Renders [stack]'s top entry and animates between entries on push / pop, with built-in
 * predictive-back gesture support driven by the system back dispatcher (Android
 * predictive back, iOS start-edge swipe) via
 * [androidx.navigationevent.compose.NavigationBackHandler].
 *
 * ## How the gesture flow works
 * 1. While a gesture is in flight, the dispatcher emits [NavigationEventTransitionState.InProgress]
 *    events. The container reveals [previousScreenFor]'s result and seeks the transition by
 *    the finger position (derived from `touchX` and the container's measured width).
 * 2. When the gesture ends, the dispatcher returns to [NavigationEventTransitionState.Idle].
 *    The container can't tell cancel from complete from that signal alone — the topmost
 *    handler's callbacks aren't necessarily this container's. So it **speculatively** animates
 *    the lifted fraction back to 0 (cancel) using [gestureResolutionSpec].
 * 3. If the gesture was actually a complete, the caller (this container's own
 *    [onBackComplete] *or* a nested screen-level handler) mutates [stack]. The reconciliation
 *    step cancels the speculative cancel and smoothly drives the fraction to 1, then settles
 *    on the new top.
 *
 * On platforms without in-flight progress (Android <14, desktop, web, atomic back button) the
 * dispatcher fires `onBackCompleted` atomically. Step 1 is skipped; the stack mutation goes
 * straight through to the regular push/pop animation.
 *
 * The container measures itself via [androidx.compose.ui.layout.onGloballyPositioned], so it
 * works correctly even when nested inside paddings, insets or a `Scaffold` — finger position
 * is mapped into container-local coordinates.
 *
 * @param stack current navigation stack; the top entry is rendered.
 * @param previousScreenFor returns the entry to reveal during a back gesture from `current`,
 *   or `null` if back should be ignored for the current entry.
 * @param onBackComplete invoked when an atomic back (or a gesture this container's handler
 *   received) needs to pop; the caller is expected to mutate [stack] in response.
 * @param modifier modifier applied to the container.
 * @param enabled global toggle. When `false`, no back handler is registered (back propagates
 *   to the parent / system fallback) and no gesture animation is shown.
 * @param onGestureProgress optional observer for the in-flight gesture fraction (`0f..1f`).
 *   Invoked every frame the gesture progresses, and once with `0f` when it resolves.
 *   Useful for driving background dim, parallax depth, etc.
 * @param onTransitionSettled optional observer invoked with the top entry each time the
 *   seekable transition settles onto a new entry (`currentState == targetState`). Fires on
 *   the initial composition, after every push/pop animation completes, and after a predictive
 *   back gesture resolves to a different entry — never mid-animation, and never twice in a
 *   row for the same entry (a cancelled gesture that lands back on the current entry does
 *   not re-fire). Useful for deferring heavy work (camera bind, network fetch, etc.) until
 *   the entering screen is fully presented.
 * @param transitionSpec [ContentTransform] factory used for forward navigation.
 * @param popTransitionSpec [ContentTransform] factory used for programmatic pops.
 * @param predictivePopTransitionSpec [ContentTransform] factory used while the back gesture
 *   is in flight and to finish/cancel after release.
 * @param gestureResolutionSpec animation spec used to animate `progress` back to `0f` (cancel)
 *   or forward to `1f` (complete). Defaults to a 300 ms `tween`.
 * @param content per-entry composable.
 */
@Composable
@Suppress("CyclomaticComplexMethod")
public fun <T : NavStackEntry<*>> PredictiveBackContainer(
    stack: NavigationStack<T>,
    previousScreenFor: (stack: NavigationStack<T>, current: T) -> T?,
    onBackComplete: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onGestureProgress: ((Float) -> Unit)? = null,
    onTransitionSettled: ((T) -> Unit)? = null,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PushTransitionSpec(),
    popTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = PopTransitionSpec(),
    predictivePopTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform =
        PredictivePopTransitionSpec(),
    gestureResolutionSpec: AnimationSpec<Float> = tween(),
    content: @Composable (T) -> Unit,
) {
    // `current` and `previous` are the only screens the seekable transition cares about.
    // `previous` is non-null iff a back gesture is in flight or being resolved.
    var current by remember { mutableStateOf(stack.screen) }
    var previous by remember { mutableStateOf<T?>(null) }
    var progress by remember { mutableFloatStateOf(0F) }
    val transitionState = remember { SeekableTransitionState(current) }
    // Holds the speculative cancel-animation so the stack-reconciliation
    // effect can interrupt it when a gesture turns out to be a complete.
    val cancelJob = remember { mutableStateOf<Job?>(null) }
    // Container left/width in window coordinates, captured via onGloballyPositioned.
    // touchX comes from the dispatcher in WINDOW coordinates; we subtract our own
    // left to land in container-local space and divide by width to get a 0..1 fraction.
    var containerLeftPx by remember { mutableIntStateOf(0) }
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val progressObserver = rememberUpdatedState(onGestureProgress)
    val settledObserver = rememberUpdatedState(onTransitionSettled)

    // Single write site for `progress`: updates the state and pings any observer
    // synchronously, so the observer doesn't need its own snapshotFlow coroutine.
    fun setProgress(value: Float) {
        progress = value
        progressObserver.value?.invoke(value)
    }

    // Fire settled directly from the SeekableTransitionState — decouples the
    // signal from composition timing so it fires uniformly for the initial
    // composition, regular push/pop animations, gesture cancels and gesture
    // completions. `distinctUntilChanged` collapses a cancel that lands back
    // on the same entry (null → E → null → E after filterNotNull becomes E).
    LaunchedEffect(transitionState) {
        snapshotFlow { transitionState.currentState.takeIf { it == transitionState.targetState } }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { settledObserver.value?.invoke(it) }
    }

    val canHandleBack = enabled && previousScreenFor(stack, current) != null
    val navState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navState,
        isBackEnabled = canHandleBack,
        // Delegate the pop entirely to the caller. The stack mutation that
        // follows triggers `LaunchedEffect(stack)` below, which uniformly
        // handles all three completion paths (atomic, gesture via this
        // handler, gesture via a nested handler).
        onBackCompleted = { onBackComplete(current) },
    )

    val owner = LocalNavigationEventDispatcherOwner.current
    // Hoist live stack/resolver into snapshot state so the long-lived gesture
    // collector reads the current values rather than a stale closure capture.
    val stackHolder = rememberUpdatedState(stack)
    val resolverHolder = rememberUpdatedState(previousScreenFor)
    val enabledHolder = rememberUpdatedState(enabled)
    LaunchedEffect(Unit) {
        // Read from `dispatcher.transitionState` (the dispatcher-level
        // StateFlow) rather than `navState.transitionState` — the latter only
        // updates on the topmost active handler. A nested screen-level handler
        // wins LIFO, and the container would otherwise miss the in-flight
        // progress entirely.
        val flow = owner?.navigationEventDispatcher?.transitionState ?: return@LaunchedEffect
        flow.collect { event ->
            when (event) {
                is NavigationEventTransitionState.InProgress -> {
                    if (!enabledHolder.value) return@collect
                    cancelJob.value?.cancel()
                    cancelJob.value = null
                    if (previous == null) {
                        previous = resolverHolder.value(stackHolder.value, current)
                            ?: return@collect
                    }
                    setProgress(fingerFraction(event.latestEvent, containerLeftPx, containerWidthPx))
                }
                NavigationEventTransitionState.Idle -> {
                    if (previous != null) {
                        cancelJob.value = launch {
                            animate(progress, 0F, animationSpec = gestureResolutionSpec) { v, _ ->
                                setProgress(v)
                            }
                            previous = null
                            cancelJob.value = null
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val pos = coordinates.positionInWindow()
            containerLeftPx = pos.x.toInt()
            containerWidthPx = coordinates.size.width
        },
    ) {
        val transition = rememberTransition(transitionState, label = "Screen transition")

        // Stack snapshot at the moment the transition's currentState last settled.
        // Used by `isPop` to detect post-gesture / programmatic pops vs pushes.
        // The stale capture is intentional — frozen baseline that only refreshes
        // when the transition settles.
        val transitionCurrentStackSnapshot = remember(transition.currentState) { stack }
        val isPop = isPop(transitionCurrentStackSnapshot, stack)

        // Reconcile external stack mutations. Three cases:
        //  • match (`prev == newTop`): caller popped to our gesture target —
        //    finish the seek smoothly, then settle.
        //  • mismatch (`prev != null` but `prev != newTop`): caller mutated
        //    elsewhere mid-gesture — smoothly run the lifted fraction back to 0,
        //    then adopt the new top (the post-gesture branch animates the
        //    push/pop into place).
        //  • no gesture (`prev == null`): plain stack change — adopt the new top.
        LaunchedEffect(stack) {
            val newTop = stack.screen
            val prev = previous
            when {
                prev != null && prev == newTop -> {
                    cancelJob.value?.cancel()
                    cancelJob.value = null
                    animate(progress, 1F, animationSpec = gestureResolutionSpec) { v, _ ->
                        setProgress(v)
                    }
                    Snapshot.withMutableSnapshot {
                        previous = null
                        progress = 0F
                    }
                    progressObserver.value?.invoke(0F)
                    transitionState.snapTo(newTop)
                    current = newTop
                }
                prev != null -> {
                    cancelJob.value?.cancel()
                    cancelJob.value = null
                    animate(progress, 0F, animationSpec = gestureResolutionSpec) { v, _ ->
                        setProgress(v)
                    }
                    Snapshot.withMutableSnapshot {
                        previous = null
                        progress = 0F
                    }
                    progressObserver.value?.invoke(0F)
                    transitionState.snapTo(current)
                    current = newTop
                }
                else -> current = newTop
            }
        }

        val prev = previous
        val targetZIndex = rememberMonotonicZIndex(
            transition = transition,
            sceneState = current,
            isPop = isPop,
            inPredictiveBack = prev != null,
        )

        val contentTransform: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
            val base = when {
                prev != null -> predictivePopTransitionSpec(this)
                isPop -> popTransitionSpec(this)
                else -> transitionSpec(this)
            }
            ContentTransform(
                targetContentEnter = base.targetContentEnter,
                initialContentExit = base.initialContentExit,
                targetContentZIndex = targetZIndex,
                sizeTransform = base.sizeTransform,
            )
        }

        if (prev != null) {
            // snapshotFlow conflates progress updates so heavy content
            // (e.g. WebView) doesn't drop frames from per-emission
            // LaunchedEffect cancel/restart.
            LaunchedEffect(prev) {
                snapshotFlow { progress }.collect { transitionState.seekTo(it, prev) }
            }
        } else {
            // No gesture in flight: drive the transition toward `current` if
            // it isn't there already (regular push/programmatic pop), or snap
            // away any leftover target from a settled gesture.
            LaunchedEffect(current) {
                when {
                    transitionState.currentState != current -> transitionState.animateTo(current)
                    transitionState.targetState != current -> transitionState.snapTo(current)
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
 * Assigns each entry a monotonic zIndex derived from the outgoing entry's current
 * value (± 1 per direction) so the current top always draws over the revealed one
 * across chained pops. Prunes the map on settle so it doesn't accumulate stale
 * entries.
 *
 * Needed because `AnimatedContent` caches each slot's `specOnEnter.targetContentZIndex`
 * on first composition: a constant pop-target zIndex eventually collides with a prior
 * pop's target, letting composition order win and dropping the outgoing screen behind
 * the incoming one.
 */
@Composable
private fun <T : NavStackEntry<*>> rememberMonotonicZIndex(
    transition: Transition<T>,
    sceneState: T,
    isPop: Boolean,
    inPredictiveBack: Boolean,
): Float {
    val zIndices = remember { mutableStateMapOf<Any, Float>() }
    val initialKey = transition.currentState.id
    val targetKey = transition.targetState.id
    val initialZIndex = zIndices.getOrPut(initialKey) { 0f }
    val targetZIndex = computeMonotonicZIndex(
        initialKey = initialKey,
        targetKey = targetKey,
        initialZIndex = initialZIndex,
        existingTargetZIndex = zIndices[targetKey],
        targetIsScene = transition.targetState == sceneState,
        isPop = isPop,
        inPredictiveBack = inPredictiveBack,
    )
    zIndices[targetKey] = targetZIndex

    LaunchedEffect(transition) {
        snapshotFlow { transition.isRunning }
            .filter { !it }
            .collect {
                val settledKey = transition.targetState.id
                val stale = zIndices.keys.filter { it != settledKey }
                stale.forEach { zIndices.remove(it) }
            }
    }
    return targetZIndex
}

/**
 * Pure zIndex arithmetic driving [rememberMonotonicZIndex]. Exposed as `internal`
 * so the invariants can be unit-tested without a Compose test harness.
 *
 * Rules (evaluated top-down):
 *  • Ongoing forward transition to an already-tracked target — reuse its assigned z
 *    so a mid-flight state change doesn't reshuffle the ordering.
 *  • initial == target (settled / trivial segment) — keep initial's z.
 *  • Pop or predictive-back — target below initial (`initial - 1`).
 *  • Push — target above initial (`initial + 1`).
 */
internal fun computeMonotonicZIndex(
    initialKey: Any,
    targetKey: Any,
    initialZIndex: Float,
    existingTargetZIndex: Float?,
    targetIsScene: Boolean,
    isPop: Boolean,
    inPredictiveBack: Boolean,
): Float = when {
    !inPredictiveBack && !targetIsScene && existingTargetZIndex != null -> existingTargetZIndex
    initialKey == targetKey -> initialZIndex
    isPop || inPredictiveBack -> initialZIndex - 1f
    else -> initialZIndex + 1f
}

/**
 * Maps the dispatcher's `touchX` (window-coordinate pixels) into a `0..1` fraction in the
 * direction the gesture progresses. Subtracting the container's left offset makes the
 * mapping correct even when the container is not full-screen. Falls back to the dispatcher's
 * pre-damped `progress` until the container has been measured.
 */
private fun fingerFraction(
    event: NavigationEvent,
    containerLeftPx: Int,
    containerWidthPx: Int,
): Float {
    if (containerWidthPx <= 0) return event.progress
    val localTouchX = event.touchX - containerLeftPx
    val raw = when (event.swipeEdge) {
        NavigationEvent.EDGE_RIGHT -> (containerWidthPx - localTouchX) / containerWidthPx
        else -> localTouchX / containerWidthPx
    }
    return raw.coerceIn(0F, 1F)
}

/**
 * Detects whether [new] is a clean subset of [old] (i.e. a pop). Compares by
 * [NavStackEntry.id] so the check works regardless of whether entries are data classes.
 *
 * Adapted from `androidx.navigation3.ui.NavDisplay.isPop`.
 */
internal fun <T : NavStackEntry<*>> isPop(old: NavigationStack<T>, new: NavigationStack<T>): Boolean {
    if (old.firstOrNull()?.id != new.firstOrNull()?.id) return false
    if (new.size > old.size) return false
    val diverging = new.indices.firstOrNull { i -> new[i].id != old[i].id }
    return diverging == null && new.size != old.size
}
