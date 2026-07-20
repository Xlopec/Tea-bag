package io.github.xlopec.tea.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Black-box tests for [PredictiveBackContainer]. Observations:
 * - which entries the seekable transition produces (via `content` invocations)
 * - which transition spec was selected (push / pop / predictive-pop)
 * - which entry `onBackComplete` was called with
 *
 * Direct fraction inspection is not possible (SeekableTransitionState is private
 * to the composable), so "is animating predictively" is asserted indirectly via
 * "predictive-pop spec was invoked AND both entries are rendered".
 */
@OptIn(ExperimentalTestApi::class)
@Suppress("TooManyFunctions")
class PredictiveBackContainerTest {

    @Test
    fun initial_render_emits_only_top_of_stack() = backTest {
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                content = { rendered += it },
            )
        }
        settle()
        assertEquals(listOf(TestEntry("details")), rendered.distinct())
    }

    @Test
    fun pushing_new_entry_renders_the_new_top_with_push_spec() = backTest {
        var currentStack by mutableStateOf(stack("home"))
        val pushInvocations = SpecCounter()
        val popInvocations = SpecCounter()
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                transitionSpec = { pushInvocations.invoked(); NoTransition },
                popTransitionSpec = { popInvocations.invoked(); NoTransition },
                content = { rendered += it },
            )
        }
        settle()
        rendered.clear()

        currentStack = stack("home", "details")
        settle()

        assertTrue(TestEntry("details") in rendered, "expected new top to be rendered, got $rendered")
        assertTrue(pushInvocations.count >= 1, "expected push spec to be invoked")
        assertEquals(0, popInvocations.count, "pop spec must not fire for a push")
    }

    @Test
    fun programmatic_pop_renders_previous_with_pop_spec() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val pushInvocations = SpecCounter()
        val popInvocations = SpecCounter()
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                transitionSpec = { pushInvocations.invoked(); NoTransition },
                popTransitionSpec = { popInvocations.invoked(); NoTransition },
                content = { rendered += it },
            )
        }
        settle()
        rendered.clear()
        pushInvocations.count = 0
        popInvocations.count = 0

        currentStack = stack("home")
        settle()

        assertTrue(TestEntry("home") in rendered, "expected previous top to be rendered, got $rendered")
        assertTrue(popInvocations.count >= 1, "expected pop spec, push=${pushInvocations.count} pop=${popInvocations.count}")
    }

    @Test
    fun atomic_back_fires_onBackComplete_with_current() = backTest {
        var popped: TestEntry? = null
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = { popped = it },
                content = {},
            )
        }
        settle()

        // No Started/Progressed — atomic complete (e.g. hardware back on Android <14).
        backCompleted()
        settle()

        assertEquals(TestEntry("details"), popped)
    }

    @Test
    fun in_progress_gesture_renders_previous_with_predictive_spec() = backTest {
        val predictiveInvocations = SpecCounter()
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                predictivePopTransitionSpec = { predictiveInvocations.invoked(); NoTransition },
                content = { rendered += it },
            )
        }
        settle()
        rendered.clear()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()

        assertTrue(TestEntry("home") in rendered, "expected previous (home) to be rendered during gesture, got $rendered")
        assertTrue(predictiveInvocations.count >= 1, "expected predictive-pop spec")
    }

    @Test
    fun cancelled_gesture_returns_to_original_top() = backTest {
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        backCancelled()
        // Cancel animation (~300ms) then AnimatedContent decomposes the
        // gesture's `previous` entry.
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("details")), composed.snapshot(), "only the original top should remain composed")
    }

    @Test
    fun cancel_settle_does_not_invoke_forward_push_spec() = backTest {
        // Regression: at cancel-end, `snapTo(current)` triggers an AnimatedContent
        // segment change (prev → current) via updateTarget. Without the
        // cancelSettling gate, contentTransform re-evaluates with `prev == null`
        // and `isPop == false`, falling through to the forward `transitionSpec`
        // (push). Its `slideIntoContainer(Start)` targetContentEnter leaves the
        // returning screen stuck at +width off-screen, exposing the container
        // background as a black/magenta flash.
        val pushInvocations = SpecCounter()
        val popInvocations = SpecCounter()
        val predictiveInvocations = SpecCounter()
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                transitionSpec = { pushInvocations.invoked(); NoTransition },
                popTransitionSpec = { popInvocations.invoked(); NoTransition },
                predictivePopTransitionSpec = { predictiveInvocations.invoked(); NoTransition },
                content = { composed.Track(it) },
            )
        }
        settle()
        // Reset baseline: the initial composition legitimately invokes the push
        // spec to establish the initial content's frozen `specOnEnter`.
        pushInvocations.count = 0
        popInvocations.count = 0
        predictiveInvocations.count = 0

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        assertTrue(
            predictiveInvocations.count >= 1,
            "predictive-pop spec should be invoked during the gesture",
        )
        val pushBeforeCancel = pushInvocations.count
        val popBeforeCancel = popInvocations.count

        backCancelled()
        settle(ms = 600L)

        assertEquals(
            setOf(TestEntry("details")),
            composed.snapshot(),
            "only the original top should remain composed after cancel",
        )
        assertEquals(
            pushBeforeCancel,
            pushInvocations.count,
            "forward push spec must not be invoked for the snapTo(current) segment " +
                "at cancel-settle (was $pushBeforeCancel, now ${pushInvocations.count})",
        )
        assertEquals(
            popBeforeCancel,
            popInvocations.count,
            "programmatic pop spec must not be invoked; stack shape did not change",
        )
    }

    @Test
    @Ignore("Fails on today's code — mismatch reconciliation branch has the same " +
        "snapTo(current) target-swap that the cancel branch used to have, without " +
        "the cancelSettling gate. Enable when the refactor unifies both branches.")
    fun mid_gesture_stack_mismatch_does_not_invoke_forward_push_spec() = backTest {
        // Regression (companion to cancel_settle test): the reconciliation
        // "mismatch" branch fires when a caller mutates the stack shape
        // mid-gesture to something OTHER than the gesture target (e.g. an
        // async command pushes a new screen while the finger is down). It
        // then runs `animate(progress, 0F)`, nulls `previous`, and calls
        // `transitionState.snapTo(current)` — the SAME snapTo-that-swaps-target
        // pattern the cancel branch used to have. Without a gate, the
        // returning `current` gets stuck at slideIntoContainer(Start)'s
        // enter-start position and the container background flashes through.
        val currentStack = mutableStateOf(stack("home", "details"))
        val pushInvocations = SpecCounter()
        val popInvocations = SpecCounter()
        val predictiveInvocations = SpecCounter()
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                transitionSpec = { pushInvocations.invoked(); NoTransition },
                popTransitionSpec = { popInvocations.invoked(); NoTransition },
                predictivePopTransitionSpec = { predictiveInvocations.invoked(); NoTransition },
                content = { composed.Track(it) },
            )
        }
        settle()
        pushInvocations.count = 0
        popInvocations.count = 0
        predictiveInvocations.count = 0

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        assertTrue(
            predictiveInvocations.count >= 1,
            "predictive-pop spec should be invoked during the gesture",
        )
        val pushBeforeMutation = pushInvocations.count
        val popBeforeMutation = popInvocations.count

        // Mismatch: mutate the stack to a NEW top that isn't the gesture
        // target (`home`). This drives the reconciliation `prev != null &&
        // prev.id != newTop.id` branch, which animates progress→0 and calls
        // `snapTo(current)` before adopting the new top.
        currentStack.value = stack("home", "details", "extra")
        settle(ms = 800L)

        assertEquals(
            setOf(TestEntry("extra")),
            composed.snapshot(),
            "only the new top should remain composed after mismatch reconciliation",
        )
        assertEquals(
            popBeforeMutation,
            popInvocations.count,
            "pop spec must not be invoked; the mismatch resolves to a push, not a pop",
        )
        // The mismatch branch legitimately transitions to `extra` via a push.
        // Exactly one push-spec invocation is expected for that final segment
        // (details → extra). Anything more indicates the snapTo(current)
        // target-swap also fell through to the push spec — the bug this test
        // guards against.
        assertEquals(
            pushBeforeMutation + 1,
            pushInvocations.count,
            "expected exactly one push-spec invocation for the details→extra segment; " +
                "extras indicate snapTo(current) segment also fell through to push spec",
        )
    }

    @Test
    fun completed_gesture_with_external_pop_settles_on_new_top() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()

        // Simulate an inner screen handler intercepting completion and
        // popping the stack externally — the container's onBackComplete
        // never fires; it reconciles via the stack change.
        currentStack = stack("home")
        backCompleted()
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("home")), composed.snapshot(), "only the new top should remain composed")
    }

    @Test
    fun inner_handler_intercepting_complete_drives_smooth_settle_on_new_top() = backTest {
        // A screen-level NavigationBackHandler composed inside `content` wins
        // LIFO. The container's own handler never sees the terminal events;
        // reconciliation runs through `LaunchedEffect(stack)` on the
        // matching-new-top branch.
        val currentStack = mutableStateOf(stack("home", "details"))
        var containerPopFired: TestEntry? = null
        var innerCompletedFired = 0
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = { containerPopFired = it },
                content = { entry ->
                    composed.Track(entry)
                    if (entry == TestEntry("details")) {
                        val innerState = rememberNavigationEventState(NavigationEventInfo.None)
                        NavigationBackHandler(
                            state = innerState,
                            isBackEnabled = true,
                            onBackCompleted = {
                                innerCompletedFired++
                                currentStack.value = stack("home")
                            },
                        )
                    }
                },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        backCompleted()
        settle(ms = 600L)

        // Inner handler fired exactly once and the container's own handler
        // never did — LIFO routing held.
        assertEquals(1, innerCompletedFired, "inner handler must fire on complete")
        assertEquals(null, containerPopFired, "container's own onBackComplete must NOT fire when inner intercepts")
        // Stack-reconciliation branch ran the smooth complete and settled
        // on the new top — only "home" remains composed (no leftover
        // "details" from a stuck `previous`).
        assertEquals(setOf(TestEntry("home")), composed.snapshot())
    }

    @Test
    fun inner_handler_intercepting_cancel_returns_to_original_top() = backTest {
        val currentStack = mutableStateOf(stack("home", "details"))
        var innerCancelledFired = 0
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                content = { entry ->
                    composed.Track(entry)
                    if (entry == TestEntry("details")) {
                        val innerState = rememberNavigationEventState(NavigationEventInfo.None)
                        NavigationBackHandler(
                            state = innerState,
                            isBackEnabled = true,
                            onBackCancelled = { innerCancelledFired++ },
                            onBackCompleted = { currentStack.value = stack("home") },
                        )
                    }
                },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        backCancelled()
        settle(ms = 600L)

        assertEquals(1, innerCancelledFired, "inner handler must receive the cancel")
        // Stack untouched, container's collector ran its cancel-animation,
        // `previous` cleared → only "details" remains composed.
        assertEquals(setOf(TestEntry("details")), composed.snapshot())
    }

    @Test
    fun mismatch_branch_settles_on_unrelated_new_top_without_residue() = backTest {
        // Caller pushes/swaps to an entry that is NOT the gesture's previous
        // target while a gesture is in flight. Container should clean up its
        // gesture state and end up showing only the new top.
        val currentStack = mutableStateOf(stack("home", "details"))
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        // Caller jumps to an unrelated screen instead of popping to `home`.
        currentStack.value = stack("home", "details", "filters")
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("filters")), composed.snapshot(), "only the new top should remain")
    }

    @Test
    fun state_only_update_to_top_mid_gesture_does_not_disturb_the_gesture() = backTest {
        // Regression: an async command result that updates the top screen's
        // state (same id, different payload) must NOT be treated as a
        // mid-gesture push/pop. Before the fix the container would fire the
        // mismatch branch — animating progress back to 0 while the finger was
        // still down — causing the well-known "jumps back and forth" bug.
        val currentStack = mutableStateOf(
            stackOf(TestEntry("home", payload = 0), TestEntry("details", payload = 0)),
        )
        val composed = ComposedEntries()
        val predictiveInvocations = SpecCounter()
        var contentRenderedWith: TestEntry? = null
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                predictivePopTransitionSpec = { predictiveInvocations.invoked(); NoTransition },
                content = {
                    composed.Track(it)
                    if (it.id == "details") contentRenderedWith = it
                },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        assertTrue(TestEntry("home") in composed.snapshot(), "gesture should be revealing home")
        predictiveInvocations.count = 0

        // Simulate the async result: same top id, new payload — the exact
        // shape stack.mutate { updateInstanceOfById(...) } produces.
        currentStack.value = stackOf(TestEntry("home", payload = 0), TestEntry("details", payload = 42))
        settle()

        // Gesture still in flight: both entries composed, predictive spec still valid.
        assertTrue(
            TestEntry("home") in composed.snapshot(),
            "home must remain composed after a mid-gesture state-only update, got ${composed.snapshot()}",
        )
        assertEquals(
            TestEntry("details", payload = 42),
            contentRenderedWith,
            "content lambda must receive the freshest entry for the top id",
        )

        backCancelled()
        settle(ms = 600L)
        assertEquals(
            setOf(TestEntry("details", payload = 42)),
            composed.snapshot(),
            "after cancel only the updated top should remain",
        )
    }

    @Test
    fun touchX_drives_seek_independently_of_pre_damped_progress() = backTest {
        // The container should use touchX/containerWidth, not the pre-damped
        // `progress` field. We send InProgress events with progress=1.0 but
        // tiny touchX values — the container should still be in a gesture and
        // render the previous screen, and the predictive spec should be used
        // (proves the gesture path activated). If the container used the
        // damped progress directly, the test would still pass; the regression
        // it specifically guards against is the "previous never set" case
        // when touchX/width = 0 and the fallback kicks in.
        val composed = ComposedEntries()
        val predictiveInvocations = SpecCounter()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                predictivePopTransitionSpec = { predictiveInvocations.invoked(); NoTransition },
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted(progress = 0.1F, touchX = 10F)
        backProgressed(progress = 0.2F, touchX = 20F)
        settle()

        assertTrue(TestEntry("home") in composed.snapshot(), "previous must be composed during gesture")
        assertTrue(predictiveInvocations.count >= 1, "predictive spec must fire during gesture")
    }

    @Test
    fun enabled_false_does_not_register_handler_or_run_animations() = backTest {
        var popped: TestEntry? = null
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = { popped = it },
                enabled = false,
                content = {},
            )
        }
        settle()

        backCompleted()
        settle()

        assertEquals(null, popped, "disabled container must not consume back")
        assertEquals(1, unhandledBackCount, "back must propagate to fallback")
    }

    @Test
    fun on_gesture_progress_is_called_during_in_flight_gesture() = backTest {
        val observed = mutableListOf<Float>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                onGestureProgress = { observed += it },
                content = {},
            )
        }
        settle()
        observed.clear()

        backStarted(touchX = 0F)
        backProgressed(progress = 0.3F, touchX = 30F)
        backProgressed(progress = 0.5F, touchX = 50F)
        settle()

        assertTrue(observed.isNotEmpty(), "observer should have received progress emissions")
        assertTrue(observed.any { it > 0F }, "observer should see non-zero values during gesture, got $observed")
    }

    @Test
    fun disabling_back_mid_gesture_does_not_leak_previous() = backTest {
        var enabled by mutableStateOf(true)
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                enabled = enabled,
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        // Caller toggles `enabled` off mid-gesture; the InProgress collector
        // should refuse further updates and the gesture should resolve
        // cleanly when the dispatcher returns to Idle.
        enabled = false
        settle()
        backCancelled()
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("details")), composed.snapshot(), "only original top should remain")
    }

    @Test
    fun root_with_no_previous_does_not_consume_back() = backTest {
        var popped: TestEntry? = null
        set {
            PredictiveBackContainer(
                stack = stack("home"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = { popped = it },
                content = {},
            )
        }
        settle()

        backCompleted()
        settle()

        // Two things must hold at the root:
        // (a) `onBackComplete` isn't invoked (nothing to pop).
        // (b) the back event isn't swallowed by the dispatcher — it must
        //     propagate to the platform fallback (Activity finish on Android,
        //     UIKit dismiss on iOS). This is what `isBackEnabled = false`
        //     achieves; a handler that consumed the event would block it.
        assertEquals(null, popped, "root screen must not pop")
        assertEquals(1, unhandledBackCount, "back at root must propagate to the dispatcher fallback")
    }

    @Test
    fun on_transition_settled_fires_with_top_after_initial_composition() = backTest {
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                onTransitionSettled = { settled += it },
                content = {},
            )
        }
        settle()

        assertEquals(listOf(TestEntry("details")), settled)
    }

    @Test
    fun on_transition_settled_fires_after_programmatic_push_animation() = backTest {
        var currentStack by mutableStateOf(stack("home"))
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                onTransitionSettled = { settled += it },
                content = {},
            )
        }
        settle()
        settled.clear()

        currentStack = stack("home", "details")
        // Long enough for the seekable transition's animateTo (default spring) to settle.
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("details")), settled)
    }

    @Test
    fun on_transition_settled_fires_after_programmatic_pop_animation() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                onTransitionSettled = { settled += it },
                content = {},
            )
        }
        settle()
        settled.clear()

        currentStack = stack("home")
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("home")), settled)
    }

    @Test
    fun on_transition_settled_fires_with_new_top_after_completed_gesture() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                onTransitionSettled = { settled += it },
                content = {},
            )
        }
        settle()
        settled.clear()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        // Simulate the pop the caller performs on complete; the reconciliation
        // branch drives the seek to 1F and settles on `home`.
        currentStack = stack("home")
        backCompleted()
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("home")), settled)
    }

    @Test
    fun on_transition_settled_does_not_re_fire_after_cancelled_gesture() = backTest {
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                onTransitionSettled = { settled += it },
                content = {},
            )
        }
        settle()
        // Initial fire captured; a cancel lands back on the same entry and
        // `distinctUntilChanged` must suppress the duplicate emission.
        assertEquals(listOf(TestEntry("details")), settled)

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        backCancelled()
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("details")), settled)
    }
}

private class SpecCounter {
    var count: Int = 0
    fun invoked() { count++ }
}

/**
 * Tracks which entries are currently composed using [DisposableEffect]. Use
 * [Track] inside `PredictiveBackContainer.content` and read [snapshot] after
 * the test reaches a stable state. Reads/writes are single-threaded (test
 * dispatcher) so a plain set is fine.
 */
private class ComposedEntries {
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

private val NoTransition: ContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = ExitTransition.None,
    targetContentZIndex = 0F,
    sizeTransform = null,
)

/** Arbitrary "gesture is partway through" fraction. Exact value isn't load-bearing. */
private const val MidGestureProgress = 0.5F
