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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Renders [stack]'s top entry and animates between entries on push / pop, with built-in
 * predictive-back gesture support driven by the system back dispatcher (Android
 * predictive back, iOS start-edge swipe) via
 * [androidx.navigationevent.compose.NavigationBackHandler].
 *
 * ## How the gesture flow works
 * 1. While a gesture is in flight, the dispatcher emits [NavigationEventTransitionState.InProgress]
 *    events. The container reveals [backTarget]'s result and seeks the transition by
 *    the finger position (derived from `touchX` and the container's measured width).
 * 2. When the gesture ends, the dispatcher returns to [NavigationEventTransitionState.Idle].
 *    The container can't tell cancel from complete from that signal alone — the topmost
 *    handler's callbacks aren't necessarily this container's. So it **speculatively** animates
 *    the lifted fraction back to 0 (cancel) using the predictive-pop [ScreenTransition]'s
 *    animation spec.
 * 3. If the gesture was actually a complete, the caller (this container's own [onBack] *or*
 *    a nested screen-level handler) mutates [stack]. The reconciliation step cancels the
 *    speculative cancel and smoothly drives the fraction to 1, then settles on the new top.
 *
 * On platforms without in-flight progress (Android <14, desktop, web, atomic back button) the
 * dispatcher fires `onBackCompleted` atomically. Step 1 is skipped; the stack mutation goes
 * straight through to the regular push/pop animation.
 *
 * The container reveals [backTarget] for any gesture the dispatcher reports, including one a
 * nested screen-level handler intercepts without popping. Return `null` from [backTarget] while
 * such a handler owns back.
 *
 * The container measures itself via [androidx.compose.ui.layout.onGloballyPositioned], so it
 * works correctly even when nested inside paddings, insets or a `Scaffold` — finger position
 * is mapped into container-local coordinates.
 *
 * @param stack current navigation stack; the top entry is rendered.
 * @param onBack invoked when an atomic back (or a gesture this container's handler received)
 *   needs to pop; the caller is expected to mutate [stack] in response.
 * @param modifier modifier applied to the container.
 * @param state observable container state, see [PredictiveBackContainerState].
 * @param backTarget returns the entry a back gesture reveals, or `null` if this container
 *   doesn't handle back for the current top (back then propagates to the parent / system
 *   fallback). The caller's [onBack] is expected to make that entry the new top.
 * @param contentKey maps an entry to the key of the content slot it renders in. Entries that
 *   share a key swap in place with no transition, and composition state under that slot
 *   survives the swap — e.g. bottom-nav tabs sharing one key keep the nav bar (and its press
 *   ripple) alive across a tab switch. A shared-key entry is never revealed by a gesture.
 *   Defaults to [NavStackEntry.id].
 * @param transition picks the [ScreenTransition] for a segment from its
 *   [ScreenTransitionScope.from] / [ScreenTransitionScope.to] entries and
 *   [ScreenTransitionScope.kind]. Called during draw, so it must be cheap and pure; return
 *   [ScreenTransition.None] to skip the animation. Defaults to [DefaultScreenTransition].
 * @param content per-entry composable.
 */
@Composable
@Suppress("CyclomaticComplexMethod", "LongMethod")
public fun <T : NavStackEntry<*>> PredictiveBackContainer(
    stack: NavigationStack<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    state: PredictiveBackContainerState<T> = rememberPredictiveBackContainerState(stack.screen),
    backTarget: (NavigationStack<T>) -> T? = { it.previousScreen },
    contentKey: (T) -> Any = { it.id },
    transition: ScreenTransitionScope<T>.() -> ScreenTransition = { DefaultScreenTransition(kind) },
    content: @Composable NavEntryScope.(T) -> Unit,
) {
    // `current` and `previous` are the only screens the seekable transition cares about.
    // `previous` is non-null iff a back gesture is in flight or being resolved.
    var current by state::current
    var previous by state::previous
    val transitionState = state.seekableState
    state.top = stack.screen
    val lastSeen = remember { mutableMapOf<Any?, T>() }
    // Holds the speculative cancel-animation so the stack-reconciliation
    // effect can interrupt it when a gesture turns out to be a complete.
    val cancelJob = remember { mutableStateOf<Job?>(null) }
    // Container left/width in window coordinates, captured via onGloballyPositioned.
    // touchX comes from the dispatcher in WINDOW coordinates; we subtract our own
    // left to land in container-local space and divide by width to get a 0..1 fraction.
    var containerLeftPx by remember { mutableIntStateOf(0) }
    var containerWidthPx by remember { mutableIntStateOf(0) }

    // Resolve back-ability against the freshest top (`stack.screen`), not the
    // animation-`current`, which is intentionally not refreshed on state-only
    // updates (same id, new payload) and would otherwise be a stale instance the
    // caller's resolver can't find in `stack` — leaving back disabled and the
    // system closing the app.
    val canHandleBack = backTarget(stack) != null
    val navState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navState,
        isBackEnabled = canHandleBack,
        // Delegate the pop entirely to the caller. The stack mutation that
        // follows triggers `LaunchedEffect(stack)` below, which uniformly
        // handles all three completion paths (atomic, gesture via this
        // handler, gesture via a nested handler).
        onBackCompleted = onBack,
    )

    val owner = LocalNavigationEventDispatcherOwner.current
    // Hoist live inputs into snapshot state so the long-lived gesture
    // collector reads the current values rather than a stale closure capture.
    val stackHolder = rememberUpdatedState(stack)
    val backTargetHolder = rememberUpdatedState(backTarget)
    val contentKeyHolder = rememberUpdatedState(contentKey)
    val transitionHolder = rememberUpdatedState(transition)

    fun predictiveSpec(from: T, to: T) =
        transitionHolder.value.resolve(from, to, ScreenTransitionKind.PredictivePop).animationSpec

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
                    cancelJob.value?.cancel()
                    cancelJob.value = null
                    val top = stackHolder.value.screen
                    val prev = previous
                        ?: backTargetHolder.value(stackHolder.value)
                            // A shared-slot entry can't be revealed beside the top; back
                            // still works, it just swaps in place on completion.
                            ?.takeIf { contentKeyHolder.value(it) != contentKeyHolder.value(top) }
                            ?.also { previous = it }
                        ?: return@collect
                    val fraction = fingerFraction(event.latestEvent, containerLeftPx, containerWidthPx)
                    // Single owner of the seek: drive transitionState directly here instead of
                    // via a concurrent collector, so a settle's terminal snapTo can't race a
                    // trailing seek that would re-target the revealed screen (and leave it as a
                    // forward-push target off-screen for one frame — a background flash).
                    launch { transitionState.seekTo(fraction.coerceIn(0F, 1F), prev) }
                }
                NavigationEventTransitionState.Idle -> {
                    val prev = previous
                    if (prev != null) {
                        cancelJob.value = launch {
                            // Cancel-settle: run the lifted fraction back to 0, then snap onto
                            // `current`. Per-frame seeks are launched; the awaited snapTo is the
                            // final mutation, and mutatorMutex cancels any trailing seek — so the
                            // segment settles on `current` with no residual target to mis-place.
                            animate(
                                transitionState.fraction,
                                0F,
                                animationSpec = predictiveSpec(current, prev),
                            ) { value, _ ->
                                if (value != 0F) launch { transitionState.seekTo(value.coerceIn(0F, 1F)) }
                            }
                            transitionState.snapTo(current)
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
        val seekableTransition = rememberTransition(transitionState, label = "Screen transition")

        // Stack snapshot at the moment the transition's currentState last settled.
        // Used by `isPop` to detect post-gesture / programmatic pops vs pushes.
        // The stale capture is intentional — frozen baseline that only refreshes
        // when the transition settles.
        val transitionCurrentStackSnapshot = remember(seekableTransition.currentState) { stack }
        LaunchedEffect(seekableTransition.currentState) {
            lastSeen.keys.retainAll(stack.map { it.id }.toSet())
        }
        val isPop = isPop(transitionCurrentStackSnapshot, stack)

        // Reconcile external stack mutations. Keyed on the ID sequence so
        // state-only updates to an entry (e.g. an async command result on the
        // outgoing screen) don't fire the mismatch branch mid-gesture. Three
        // cases:
        //  • match (`prev.id == newTop.id`): caller popped to our gesture
        //    target — finish the seek smoothly, then settle.
        //  • mismatch (`prev != null` but ids differ): caller mutated the shape
        //    elsewhere mid-gesture (push, pop-to-elsewhere) — smoothly run the
        //    lifted fraction back to 0, then adopt the new top.
        //  • no gesture (`prev == null`): plain structural change — adopt the
        //    new top.
        val structuralKey = remember(stack) { stack.map { it.id } }
        LaunchedEffect(structuralKey) {
            val newTop = stack.screen
            val prev = previous
            when {
                prev != null && prev.id == newTop.id -> {
                    cancelJob.value?.cancel()
                    cancelJob.value = null
                    animate(
                        transitionState.fraction,
                        1F,
                        animationSpec = predictiveSpec(current, prev),
                    ) { value, _ ->
                        if (value != 1F) launch { transitionState.seekTo(value.coerceIn(0F, 1F)) }
                    }
                    transitionState.snapTo(newTop)
                    previous = null
                    current = newTop
                }
                prev != null -> {
                    cancelJob.value?.cancel()
                    cancelJob.value = null
                    animate(
                        transitionState.fraction,
                        0F,
                        animationSpec = predictiveSpec(current, prev),
                    ) { value, _ ->
                        if (value != 0F) launch { transitionState.seekTo(value.coerceIn(0F, 1F)) }
                    }
                    transitionState.snapTo(current)
                    previous = null
                    current = newTop
                }
                else -> current = newTop
            }
        }

        val prev = previous
        val targetZIndex = rememberMonotonicZIndex(
            transition = seekableTransition,
            sceneState = current,
            isPop = isPop,
            inPredictiveBack = prev != null,
        )

        // Kind of a programmatic segment, used for its `animationSpec` when a push/pop
        // drives `fraction` below. (Placement geometry is chosen in the graphicsLayer
        // from `previous` read live, so it stays in sync with the imperatively-driven seek.)
        val programmaticKind = if (isPop) ScreenTransitionKind.Pop else ScreenTransitionKind.Push

        val contentTransform: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None,
                targetContentZIndex = targetZIndex,
                sizeTransform = null,
            )
        }

        if (prev == null) {
            // No gesture in flight: drive `fraction` toward `current` with the active
            // spec's timing so the graphicsLayer slide plays for a regular push /
            // programmatic pop, or snap away any leftover target from a settled gesture.
            // In-flight gesture seeking is driven imperatively from the dispatcher handler.
            LaunchedEffect(current) {
                val from = transitionState.currentState
                when {
                    // Same slot: nothing to animate, swap in place.
                    from != current && contentKey(from) == contentKey(current) ->
                        transitionState.snapTo(current)
                    from != current -> {
                        val spec = transition.resolve(from, current, programmaticKind).animationSpec
                        animate(0F, 1F, animationSpec = spec) { value, _ ->
                            launch { transitionState.seekTo(value, current) }
                        }
                        transitionState.snapTo(current)
                    }
                    transitionState.targetState != current -> transitionState.snapTo(current)
                }
            }
        }

        seekableTransition.AnimatedContent(
            transitionSpec = contentTransform,
            contentKey = contentKey,
        ) { animatedScreen ->
            // Resolve to the freshest entry from the current stack so
            // state-only updates (which no longer trigger reconciliation)
            // still reach the composable. An entry that has already left the
            // stack but is still animating out keeps its last seen state:
            // AnimatedContent caches the first T it met for a key, which may be
            // many updates stale by the time the entry is popped.
            val fresh = stack.firstOrNull { it.id == animatedScreen.id }
                ?.also { lastSeen[it.id] = it }
                ?: lastSeen[animatedScreen.id]
                ?: animatedScreen
            val entryScope = remember(this, state, fresh.id) { NavEntryScopeImpl(this, state, fresh.id) }
            Box(
                modifier = Modifier.graphicsLayer {
                    val currentState = transitionState.currentState
                    val targetState = transitionState.targetState
                    if (contentKey(currentState) != contentKey(targetState)) {
                        val role = if (contentKey(animatedScreen) == contentKey(targetState)) {
                            ScreenRole.Incoming
                        } else {
                            ScreenRole.Outgoing
                        }
                        // Select the kind from `previous` read live at draw time, not from a
                        // composition-captured flag: the imperative seek can move transitionState
                        // a frame ahead of composition, and a stale flag would misclassify a
                        // back-reveal as a forward push (placing the incoming screen off-screen).
                        val kind = when {
                            previous != null -> ScreenTransitionKind.PredictivePop
                            isPop -> ScreenTransitionKind.Pop
                            else -> ScreenTransitionKind.Push
                        }
                        transition.resolve(currentState, targetState, kind)
                            .placement(this, role, transitionState.fraction)
                    }
                },
            ) {
                entryScope.content(fresh)
            }
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
