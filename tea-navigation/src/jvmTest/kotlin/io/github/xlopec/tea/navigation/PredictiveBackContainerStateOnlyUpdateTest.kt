package io.github.xlopec.tea.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * State-only updates: the top entry keeps its id but gets a new payload. They must
 * reach the content lambda, must not be mistaken for a structural change, and must
 * survive the entry leaving the stack.
 */
@OptIn(ExperimentalTestApi::class)
class PredictiveBackContainerStateOnlyUpdateTest {

    @Test
    fun popped_entry_animates_out_with_its_latest_state() = backTest {
        var currentStack by mutableStateOf(stackOf(TestEntry("home"), listOf(TestEntry("details", payload = 1))))
        val rendered = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                previousScreenFor = PreviousIsSecondFromTop,
                onBackComplete = {},
                content = { rendered += it },
            )
        }
        settle()
        currentStack = stackOf(TestEntry("home"), listOf(TestEntry("details", payload = 2)))
        settle()
        rendered.clear()

        currentStack = stack("home")
        settle()

        assertTrue(TestEntry("details", payload = 2) in rendered, "expected latest details state while animating out, got $rendered")
        assertTrue(TestEntry("details", payload = 1) !in rendered, "stale details state rendered while animating out: $rendered")
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
                predictivePopTransitionSpec = counting(predictiveInvocations),
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
    fun state_only_update_to_top_keeps_back_enabled() = backTest {
        // Regression: a state-only update to the top (same id, new payload) does not
        // refresh the container's internal `current`, so a resolver that looks the
        // current entry up by value in the stack would fail to find the stale instance
        // and report "no previous" — disabling back and letting the system close the app.
        // The container must resolve back-ability against the freshest top.
        var currentStack by mutableStateOf(
            stackOf(TestEntry("home"), TestEntry("details")),
        )
        var backHandled = false
        set {
            PredictiveBackContainer(
                stack = currentStack,
                // App-style resolver: locates the current entry in the stack by value.
                previousScreenFor = { s, current ->
                    val idx = s.indexOf(current)
                    if (idx > 0) s[idx - 1] else null
                },
                onBackComplete = { backHandled = true },
                content = {},
            )
        }
        settle()

        // Same id, new payload — a `stack.mutate { updateInstanceOfById(...) }`-shaped
        // change. Structural key (ids) is unchanged, so `current` stays the old instance.
        currentStack = stackOf(TestEntry("home"), TestEntry("details", payload = 42))
        settle()

        backCompleted()
        settle()

        assertEquals(
            0,
            unhandledBackCount,
            "back must stay enabled after a state-only top update (must not reach the fallback)",
        )
        assertTrue(backHandled, "container's back handler must fire, not the system fallback")
    }
}
