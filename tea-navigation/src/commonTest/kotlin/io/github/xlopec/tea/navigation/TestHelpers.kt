package io.github.xlopec.tea.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

/** Minimal NavStackEntry whose id is the value itself. */
internal data class TestEntry(override val id: String) : NavStackEntry<String>

/** Stack from string ids. The first id is the bottom, the last is the top. */
internal fun stack(vararg ids: String): NavigationStack<TestEntry> =
    stackOf(TestEntry(ids.first()), ids.drop(1).map(::TestEntry))

/** Default resolver: previous = second-from-top of the stack, null at the root. */
internal val PreviousIsSecondFromTop: (NavigationStack<TestEntry>, TestEntry) -> TestEntry? =
    { stack, _ -> stack.getOrNull(stack.lastIndex - 1) }

/**
 * Runs a Compose UI test with a fresh [NavigationEventDispatcher] connected
 * via [DirectNavigationEventInput]. The test body uses [BackTestScope] to
 * mount content under the test dispatcher and to dispatch gesture events.
 */
@OptIn(ExperimentalTestApi::class)
internal fun backTest(body: BackTestScope.() -> Unit) = runComposeUiTest {
    BackTestScope(this).body()
}

@OptIn(ExperimentalTestApi::class)
internal class BackTestScope(val ui: ComposeUiTest) {
    private val input = DirectNavigationEventInput()
    /**
     * Increments when a back event completes and no registered handler claims
     * it. Mirrors what the platform host's terminal behavior would be (e.g.
     * the Activity finishing on Android). Use [unhandledBackCount] to assert
     * that the container correctly *did not* swallow a back event.
     */
    var unhandledBackCount: Int = 0
        private set
    private val owner = object : NavigationEventDispatcherOwner {
        override val navigationEventDispatcher: NavigationEventDispatcher =
            NavigationEventDispatcher(onBackCompletedFallback = { unhandledBackCount++ })
                .also { it.addInput(input) }
    }

    /** Set the composition's content under our test dispatcher owner. */
    fun set(content: @Composable () -> Unit) {
        ui.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                content()
            }
        }
    }

    fun backStarted(progress: Float = 0F, touchX: Float = 0F) {
        input.backStarted(NavigationEvent(swipeEdge = NavigationEvent.EDGE_LEFT, progress = progress, touchX = touchX))
    }

    fun backProgressed(progress: Float, touchX: Float = 0F) {
        input.backProgressed(NavigationEvent(swipeEdge = NavigationEvent.EDGE_LEFT, progress = progress, touchX = touchX))
    }

    fun backCompleted() = input.backCompleted()
    fun backCancelled() = input.backCancelled()

    /**
     * Pump the test clock for [ms] milliseconds so any animation driven by
     * `withFrameNanos` makes progress.
     */
    fun settle(ms: Long = 500L) {
        ui.mainClock.advanceTimeBy(ms)
        ui.waitForIdle()
    }
}
