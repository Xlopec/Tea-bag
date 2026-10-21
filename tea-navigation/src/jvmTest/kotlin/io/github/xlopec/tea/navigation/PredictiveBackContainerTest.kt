package io.github.xlopec.tea.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Black-box tests for [PredictiveBackContainer]. Observations:
 * - which entries the seekable transition produces (via `content` invocations)
 * - which transition spec was selected (push / pop / predictive-pop)
 * - whether `onBack` fired, and what [PredictiveBackContainerState] / [NavEntryScope] report
 */
@Suppress("TooManyFunctions")
class PredictiveBackContainerTest {

    @Test
    fun initial_render_emits_only_top_of_stack() = backTest {
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
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
                onBack = {},
                transition = countingTransitions(push = pushInvocations, pop = popInvocations),
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
                onBack = {},
                transition = countingTransitions(push = pushInvocations, pop = popInvocations),
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
    fun atomic_back_fires_onBack() = backTest {
        var backs = 0
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = { backs++ },
                content = {},
            )
        }
        settle()

        // No Started/Progressed — atomic complete (e.g. hardware back on Android <14).
        backCompleted()
        settle()

        assertEquals(1, backs)
        assertEquals(0, unhandledBackCount)
    }

    @Test
    fun in_progress_gesture_renders_previous_with_predictive_spec() = backTest {
        val predictiveInvocations = SpecCounter()
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                transition = countingTransitions(predictive = predictiveInvocations),
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
                onBack = {},
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
        // Regression: a cancelled gesture must settle back onto the original top
        // without the forward push spec ever becoming the active segment. On the
        // legacy AnimatedContent-owned slide, the cancel `snapTo(current)` let the
        // push spec's `slideIntoContainer(Start)` enter reset the returning screen
        // to +width off-screen, flashing the container background. The predictive
        // spec is the only one that may drive the cancel.
        val pushInvocations = SpecCounter()
        val popInvocations = SpecCounter()
        val predictiveInvocations = SpecCounter()
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                transition = countingTransitions(
                    push = pushInvocations,
                    pop = popInvocations,
                    predictive = predictiveInvocations,
                ),
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
            "predictive-pop spec should be active during the gesture",
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
    fun mid_gesture_stack_mismatch_resolves_to_new_top_without_pop() = backTest {
        // The reconciliation "mismatch" branch fires when a caller mutates the
        // stack shape mid-gesture to something OTHER than the gesture target (e.g.
        // an async command pushes a new screen while the finger is down). It runs
        // the lifted fraction back to 0, settles onto the gesture's current top,
        // then adopts the new top via a fresh push. The predictive spec drives the
        // gesture; the resolution is a push, never a pop; and no residue of the
        // gesture's previous screen is left composed.
        val currentStack = mutableStateOf(stack("home", "details"))
        val pushInvocations = SpecCounter()
        val popInvocations = SpecCounter()
        val predictiveInvocations = SpecCounter()
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                onBack = {},
                transition = countingTransitions(
                    push = pushInvocations,
                    pop = popInvocations,
                    predictive = predictiveInvocations,
                ),
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
            "predictive-pop spec should be active during the gesture",
        )

        currentStack.value = stack("home", "details", "extra")
        settle(ms = 800L)

        assertEquals(
            setOf(TestEntry("extra")),
            composed.snapshot(),
            "only the new top should remain composed after mismatch reconciliation",
        )
        assertEquals(
            0,
            popInvocations.count,
            "pop spec must not drive the resolution; a mid-gesture push resolves to a push",
        )
    }

    @Test
    fun completed_gesture_with_external_pop_settles_on_new_top() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()

        // Simulate an inner screen handler intercepting completion and
        // popping the stack externally — the container's onBack
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
        var containerPopFired = false
        var innerCompletedFired = 0
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = currentStack.value,
                onBack = { containerPopFired = true },
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

        assertEquals(1, innerCompletedFired, "inner handler must fire on complete")
        assertEquals(false, containerPopFired, "container's own onBack must NOT fire when inner intercepts")
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
                onBack = {},
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
                onBack = {},
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        currentStack.value = stack("home", "details", "filters")
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("filters")), composed.snapshot(), "only the new top should remain")
    }

    @Test
    fun touchX_drives_seek_independently_of_pre_damped_progress() = backTest {
        // Guards the "previous never set" case: the seek follows touchX / width,
        // not the dispatcher's pre-damped `progress`.
        val composed = ComposedEntries()
        val predictiveInvocations = SpecCounter()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                transition = countingTransitions(predictive = predictiveInvocations),
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
    fun null_back_target_does_not_register_handler_or_run_animations() = backTest {
        var backs = 0
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = { backs++ },
                backTarget = { null },
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        assertEquals(setOf(TestEntry("details")), composed.snapshot(), "no reveal without a back target")

        backCompleted()
        settle()

        assertEquals(0, backs, "disabled container must not consume back")
        assertEquals(1, unhandledBackCount, "back must propagate to fallback")
    }

    @Test
    fun disabling_back_mid_gesture_does_not_leak_previous() = backTest {
        var enabled by mutableStateOf(true)
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                backTarget = { if (enabled) it.previousScreen else null },
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        // Caller withdraws the back target mid-gesture; the gesture should
        // resolve cleanly when the dispatcher returns to Idle.
        enabled = false
        settle()
        backCancelled()
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("details")), composed.snapshot(), "only original top should remain")
    }

    @Test
    fun root_with_no_previous_does_not_consume_back() = backTest {
        var backs = 0
        set {
            PredictiveBackContainer(
                stack = stack("home"),
                onBack = { backs++ },
                content = {},
            )
        }
        settle()

        backCompleted()
        settle()

        // Two things must hold at the root:
        // (a) `onBack` isn't invoked (nothing to pop).
        // (b) the back event isn't swallowed by the dispatcher — it must
        //     propagate to the platform fallback (Activity finish on Android,
        //     UIKit dismiss on iOS). This is what `isBackEnabled = false`
        //     achieves; a handler that consumed the event would block it.
        assertEquals(0, backs, "root screen must not pop")
        assertEquals(1, unhandledBackCount, "back at root must propagate to the dispatcher fallback")
    }

    @Test
    fun entries_sharing_a_content_key_swap_in_place_without_disposing_the_slot() = backTest {
        var currentStack by mutableStateOf(stack("feed"))
        val pushInvocations = SpecCounter()
        val rendered = mutableListOf<TestEntry>()
        var slotCompositions = 0
        var slotDisposals = 0
        set {
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                transition = countingTransitions(push = pushInvocations),
                contentKey = { if (it.id == "details") it.id else "tabs" },
                content = {
                    rendered += it
                    DisposableEffect(Unit) {
                        slotCompositions++
                        onDispose { slotDisposals++ }
                    }
                },
            )
        }
        settle()
        rendered.clear()
        pushInvocations.count = 0

        // Tab switch: same key, so the existing slot is rebound to the new entry.
        currentStack = stack("favorite")
        settle()

        assertTrue(TestEntry("favorite") in rendered, "expected new tab to be rendered, got $rendered")
        assertEquals(1, slotCompositions, "shared-key swap must reuse the slot")
        assertEquals(0, slotDisposals, "shared-key swap must not dispose the slot")
        assertEquals(0, pushInvocations.count, "shared-key swap must not run a transition")

        // A push out of the shared slot still goes through the regular push path.
        pushInvocations.count = 0
        rendered.clear()
        currentStack = stack("favorite", "details")
        settle()

        assertTrue(TestEntry("details") in rendered, "expected pushed entry to be rendered, got $rendered")
        assertTrue(pushInvocations.count >= 1, "expected push spec for a distinct key")
        assertEquals(2, slotCompositions, "distinct key must open a new slot")
    }
}
