package io.github.xlopec.tea.navigation

import androidx.compose.animation.EnterExitState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Suppress("TooManyFunctions")
class PredictiveBackContainerStateTest {

    @Test
    fun initial_composition_is_settled_on_the_top() = backTest {
        lateinit var state: PredictiveBackContainerState<TestEntry>
        val settled = mutableListOf<TestEntry>()
        var topVisibility: EnterExitState? = null
        set {
            state = rememberPredictiveBackContainerState(TestEntry("details"))
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                state = state,
                content = { entry ->
                    LaunchedEffect(isSettled) { if (isSettled) settled += entry }
                    topVisibility = animatedVisibilityScope.transition.targetState
                },
            )
        }
        settle()

        assertTrue(state.isSettled)
        assertEquals(TestEntry("details"), state.transitionState.currentState)
        assertNull(state.gestureTarget)
        assertEquals(listOf(TestEntry("details")), settled)
        assertEquals(EnterExitState.Visible, topVisibility)
    }

    @Test
    fun programmatic_push_settles_on_the_new_top() = backTest {
        var currentStack by mutableStateOf(stack("home"))
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                content = { entry -> LaunchedEffect(isSettled) { if (isSettled) settled += entry } },
            )
        }
        settle()
        settled.clear()

        currentStack = stack("home", "details")
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("details")), settled)
    }

    @Test
    fun programmatic_pop_settles_on_the_new_top() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                content = { entry -> LaunchedEffect(isSettled) { if (isSettled) settled += entry } },
            )
        }
        settle()
        settled.clear()

        currentStack = stack("home")
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("home")), settled)
    }

    @Test
    fun completed_gesture_settles_on_the_new_top() = backTest {
        var currentStack by mutableStateOf(stack("home", "details"))
        val settled = mutableListOf<TestEntry>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                content = { entry -> LaunchedEffect(isSettled) { if (isSettled) settled += entry } },
            )
        }
        settle()
        settled.clear()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        currentStack = stack("home")
        backCompleted()
        settle(ms = 1500L)

        assertEquals(listOf(TestEntry("home")), settled)
    }

    @Test
    fun gesture_unsettles_the_top_and_cancel_resettles_it() = backTest {
        // Both edges matter: callers pause heavy work (camera, scanners) the instant a
        // gesture starts and resume only after it fully resolves.
        lateinit var state: PredictiveBackContainerState<TestEntry>
        val observed = mutableMapOf<String, Boolean>()
        set {
            state = rememberPredictiveBackContainerState(TestEntry("details"))
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                state = state,
                content = { entry ->
                    val settled = isSettled
                    SideEffect { observed[entry.id] = settled }
                },
            )
        }
        settle()
        assertEquals(true, observed["details"])

        backStarted(touchX = 0F)
        backProgressed(progress = 0.3F, touchX = 30F)
        backProgressed(progress = 0.5F, touchX = 50F)
        settle()

        assertEquals(TestEntry("home"), state.gestureTarget)
        assertFalse(state.isSettled)
        assertTrue(state.fraction > 0F, "fraction should follow the finger, got ${state.fraction}")
        assertEquals(false, observed["details"])
        assertEquals(false, observed["home"], "the revealed entry isn't settled either")

        backCancelled()
        settle(ms = 1500L)

        assertNull(state.gestureTarget)
        assertTrue(state.isSettled)
        assertEquals(true, observed["details"])
    }

    @Test
    fun outgoing_top_is_unsettled_on_the_first_frame_of_a_push() = backTest {
        // The settled signal must not lag the stack: a scanner on the old top must stop
        // reporting results in the very frame the stack moves on.
        var currentStack by mutableStateOf(stack("home"))
        lateinit var state: PredictiveBackContainerState<TestEntry>
        val observed = mutableMapOf<String, Boolean>()
        set {
            state = rememberPredictiveBackContainerState(TestEntry("home"))
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                state = state,
                content = { entry ->
                    val settled = isSettled
                    SideEffect { observed[entry.id] = settled }
                },
            )
        }
        settle()
        assertEquals(true, observed["home"])

        ui.mainClock.autoAdvance = false
        currentStack = stack("home", "details")
        ui.mainClock.advanceTimeByFrame()

        assertFalse(state.isSettled)
        assertEquals(false, observed["home"])

        ui.mainClock.autoAdvance = true
        settle(ms = 1500L)
        assertTrue(state.isSettled)
        assertEquals(true, observed["details"])
    }

    @Test
    fun transition_receives_from_to_and_event_of_each_segment() = backTest {
        var currentStack by mutableStateOf(stack("home"))
        val segments = mutableSetOf<Triple<String, String, StackEvent>>()
        set {
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                transition = {
                    segments += Triple(from.id, to.id, event)
                    DefaultScreenTransition(event)
                },
                content = {},
            )
        }
        settle()

        currentStack = stack("home", "details")
        settle(ms = 1500L)
        assertTrue(Triple("home", "details", StackEvent.Push) in segments, "$segments")

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        assertTrue(Triple("details", "home", StackEvent.PredictivePop) in segments, "$segments")
        backCancelled()
        settle(ms = 1500L)

        segments.clear()
        currentStack = stack("home")
        settle(ms = 1500L)
        assertEquals(setOf(Triple("details", "home", StackEvent.Pop)), segments)
    }

    @Test
    fun transition_can_skip_the_animation_per_segment() = backTest {
        var currentStack by mutableStateOf(stack("home"))
        lateinit var state: PredictiveBackContainerState<TestEntry>
        val pushInvocations = SpecCounter()
        set {
            state = rememberPredictiveBackContainerState(TestEntry("home"))
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                state = state,
                transition = {
                    if (to.id == "filters") ScreenTransition.None else counting(pushInvocations)
                },
                content = {},
            )
        }
        settle()

        ui.mainClock.autoAdvance = false
        currentStack = stack("home", "filters")
        repeat(times = 5) { ui.mainClock.advanceTimeByFrame() }

        assertTrue(state.isSettled, "a None transition settles within a few frames")
        assertEquals(TestEntry("filters"), state.transitionState.currentState)
        assertEquals(0, pushInvocations.count)

        ui.mainClock.autoAdvance = true
        currentStack = stack("home", "filters", "details")
        settle(ms = 1500L)
        assertTrue(pushInvocations.count >= 1, "other segments still animate")
    }

    @Test
    fun shared_slot_swap_settles_without_waiting_for_a_transition() = backTest {
        var currentStack by mutableStateOf(stack("feed"))
        lateinit var state: PredictiveBackContainerState<TestEntry>
        set {
            state = rememberPredictiveBackContainerState(TestEntry("feed"))
            PredictiveBackContainer(
                stack = currentStack,
                onBack = {},
                state = state,
                contentKey = { "tabs" },
                content = {},
            )
        }
        settle()

        ui.mainClock.autoAdvance = false
        currentStack = stack("favorite")
        repeat(times = 5) { ui.mainClock.advanceTimeByFrame() }

        assertTrue(state.isSettled, "a tab switch must not stay unsettled for a transition's duration")
        assertEquals(TestEntry("favorite"), state.transitionState.currentState)
    }

    @Test
    fun shared_slot_entry_is_not_revealed_by_a_gesture_but_back_still_pops() = backTest {
        lateinit var state: PredictiveBackContainerState<TestEntry>
        val composed = ComposedEntries()
        var backs = 0
        set {
            state = rememberPredictiveBackContainerState(TestEntry("favorite"))
            PredictiveBackContainer(
                stack = stack("feed", "favorite"),
                onBack = { backs++ },
                state = state,
                contentKey = { "tabs" },
                content = { composed.Track(it) },
            )
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()

        assertNull(state.gestureTarget)
        assertEquals(setOf(TestEntry("favorite")), composed.snapshot())

        backCompleted()
        settle()
        assertEquals(1, backs)
    }

    @Test
    fun swapping_the_state_moves_gestures_to_the_new_instance() = backTest {
        val first = PredictiveBackContainerState(TestEntry("details"))
        val second = PredictiveBackContainerState(TestEntry("details"))
        var state by mutableStateOf(first)
        val composed = ComposedEntries()
        set {
            PredictiveBackContainer(
                stack = stack("home", "details"),
                onBack = {},
                state = state,
                content = { composed.Track(it) },
            )
        }
        settle()

        state = second
        settle()
        backStarted()
        backProgressed(MidGestureProgress)
        settle()

        assertEquals(TestEntry("home"), second.gestureTarget, "the gesture must drive the current state")
        assertNull(first.gestureTarget, "the replaced state must not be touched")
        assertTrue(TestEntry("home") in composed.snapshot(), "the reveal must be rendered")

        backCancelled()
        settle(ms = 1500L)

        assertTrue(second.isSettled)
        assertEquals(setOf(TestEntry("details")), composed.snapshot())
    }

    @Test
    fun hoisted_state_recovers_when_the_container_leaves_mid_gesture() = backTest {
        var shown by mutableStateOf(true)
        lateinit var state: PredictiveBackContainerState<TestEntry>
        val composed = ComposedEntries()
        set {
            state = rememberPredictiveBackContainerState(TestEntry("details"))
            if (shown) {
                PredictiveBackContainer(
                    stack = stack("home", "details"),
                    onBack = {},
                    state = state,
                    content = { composed.Track(it) },
                )
            }
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress)
        settle()
        assertEquals(TestEntry("home"), state.gestureTarget)

        shown = false
        settle()
        backCancelled()
        settle()
        shown = true
        settle(ms = 1500L)

        assertNull(state.gestureTarget)
        assertTrue(state.isSettled)
        assertEquals(setOf(TestEntry("details")), composed.snapshot())
    }
}
