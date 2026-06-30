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
        backProgressed(0.5F)
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
        backProgressed(0.3F)
        settle()
        backCancelled()
        // Cancel animation (~300ms) then AnimatedContent decomposes the
        // gesture's `previous` entry.
        settle(ms = 600L)

        assertEquals(setOf(TestEntry("details")), composed.snapshot(), "only the original top should remain composed")
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
        backProgressed(0.6F)
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
        // Simulates the real-world pattern where a screen-level
        // NavigationBackHandler (e.g. ArticleDetails) is composed inside the
        // container's `content` and wins LIFO. The container's own handler
        // never sees the terminal events; reconciliation runs through
        // `LaunchedEffect(stack)` on the matching-new-top branch.
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
        backProgressed(0.6F)
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
        backProgressed(0.4F)
        settle()
        backCancelled()
        settle(ms = 600L)

        assertEquals(1, innerCancelledFired, "inner handler must receive the cancel")
        // Stack untouched, container's collector ran its cancel-animation,
        // `previous` cleared → only "details" remains composed.
        assertEquals(setOf(TestEntry("details")), composed.snapshot())
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
