package io.github.xlopec.tea.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ScreenTransitionLayoutDirectionTest {

    @Test
    fun push_enters_from_the_right_in_ltr() = backTest {
        val (incoming, outgoing) = midPushOffsets(LayoutDirection.Ltr)

        assertTrue(incoming > 0.dp, "incoming should start off the right edge, was at $incoming")
        assertTrue(outgoing < 0.dp, "outgoing should parallax to the left, was at $outgoing")
    }

    @Test
    fun push_enters_from_the_left_in_rtl() = backTest {
        val (incoming, outgoing) = midPushOffsets(LayoutDirection.Rtl)

        assertTrue(incoming < 0.dp, "incoming should start off the left edge, was at $incoming")
        assertTrue(outgoing > 0.dp, "outgoing should parallax to the right, was at $outgoing")
    }

    @Test
    fun predictive_pop_reveals_toward_the_trailing_edge_in_rtl() = backTest {
        set {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(Modifier.size(400.dp)) {
                    PredictiveBackContainer(
                        stack = stack("home", "details"),
                        onBack = {},
                        modifier = Modifier.fillMaxSize(),
                    ) { entry -> Box(Modifier.fillMaxSize().testTag(entry.id)) }
                }
            }
        }
        settle()

        backStarted()
        backProgressed(MidGestureProgress, touchX = 200F)
        settle()

        val outgoing = ui.onNodeWithTag("details").getUnclippedBoundsInRoot().left
        val incoming = ui.onNodeWithTag("home").getUnclippedBoundsInRoot().left
        assertTrue(outgoing < 0.dp, "the popped screen should slide to the left, was at $outgoing")
        assertTrue(incoming > 0.dp, "the revealed screen should return from the right, was at $incoming")
    }

    // Left edges of the incoming (details) and outgoing (home) screens partway through a push.
    private fun BackTestScope.midPushOffsets(direction: LayoutDirection): Pair<Dp, Dp> {
        var currentStack by mutableStateOf(stack("home"))
        set {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                Box(Modifier.size(400.dp)) {
                    PredictiveBackContainer(
                        stack = currentStack,
                        onBack = {},
                        modifier = Modifier.fillMaxSize(),
                    ) { entry -> Box(Modifier.fillMaxSize().testTag(entry.id)) }
                }
            }
        }
        settle()

        ui.mainClock.autoAdvance = false
        currentStack = stack("home", "details")
        repeat(times = 8) { ui.mainClock.advanceTimeByFrame() }

        return ui.onNodeWithTag("details").getUnclippedBoundsInRoot().left to
            ui.onNodeWithTag("home").getUnclippedBoundsInRoot().left
    }
}
