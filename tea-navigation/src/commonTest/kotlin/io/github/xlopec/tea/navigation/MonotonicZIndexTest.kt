package io.github.xlopec.tea.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonotonicZIndexTest {

    @Test
    fun push_places_target_one_above_initial() {
        val z = computeMonotonicZIndex(
            initialKey = "a",
            targetKey = "b",
            initialZIndex = 0f,
            existingTargetZIndex = null,
            targetIsScene = true,
            isPop = false,
            inPredictiveBack = false,
        )
        assertEquals(1f, z)
    }

    @Test
    fun pop_places_target_one_below_initial() {
        val z = computeMonotonicZIndex(
            initialKey = "b",
            targetKey = "a",
            initialZIndex = 1f,
            existingTargetZIndex = null,
            targetIsScene = true,
            isPop = true,
            inPredictiveBack = false,
        )
        assertEquals(0f, z)
    }

    @Test
    fun predictive_back_places_target_one_below_initial() {
        val z = computeMonotonicZIndex(
            initialKey = "b",
            targetKey = "a",
            initialZIndex = 1f,
            existingTargetZIndex = null,
            targetIsScene = false,
            isPop = false,
            inPredictiveBack = true,
        )
        assertEquals(0f, z)
    }

    @Test
    fun same_key_reuses_initial_z() {
        val z = computeMonotonicZIndex(
            initialKey = "a",
            targetKey = "a",
            initialZIndex = 3f,
            existingTargetZIndex = null,
            targetIsScene = true,
            isPop = false,
            inPredictiveBack = false,
        )
        assertEquals(3f, z)
    }

    @Test
    fun ongoing_forward_transition_reuses_target_z() {
        // Mid-flight forward transition: target already assigned a z earlier — keep it,
        // don't reshuffle. Requires: not in predictive back, target != scene, target in map.
        val z = computeMonotonicZIndex(
            initialKey = "a",
            targetKey = "b",
            initialZIndex = 0f,
            existingTargetZIndex = 7f,
            targetIsScene = false,
            isPop = false,
            inPredictiveBack = false,
        )
        assertEquals(7f, z)
    }

    @Test
    fun ongoing_forward_ignores_existing_z_when_target_is_scene() {
        // If target *is* the current scene, this isn't an in-flight forward transition:
        // fall through and reassign (push).
        val z = computeMonotonicZIndex(
            initialKey = "a",
            targetKey = "b",
            initialZIndex = 0f,
            existingTargetZIndex = 7f,
            targetIsScene = true,
            isPop = false,
            inPredictiveBack = false,
        )
        assertEquals(1f, z)
    }

    @Test
    fun predictive_back_ignores_existing_z() {
        // A gesture must always recompute below the initial — don't reuse a stale value.
        val z = computeMonotonicZIndex(
            initialKey = "b",
            targetKey = "a",
            initialZIndex = 1f,
            existingTargetZIndex = 99f,
            targetIsScene = false,
            isPop = false,
            inPredictiveBack = true,
        )
        assertEquals(0f, z)
    }

    @Test
    fun chained_pops_do_not_produce_ties() {
        // Regression: with a constant `targetContentZIndex = -1f` in the transition spec,
        // chained pops repeatedly assigned -1 to each new target, so a re-created outgoing
        // slot ended up sharing z with the incoming one and composition order (not z)
        // decided draw order — flipping the "current" screen behind the revealed one.
        // With the monotonic rule, every consecutive pop must be strictly below its
        // predecessor.
        val stack = mutableMapOf<String, Float>()

        fun step(initial: String, target: String, isPop: Boolean, inPredictive: Boolean) {
            val z = computeMonotonicZIndex(
                initialKey = initial,
                targetKey = target,
                initialZIndex = stack.getOrPut(initial) { 0f },
                existingTargetZIndex = stack[target],
                targetIsScene = false,
                isPop = isPop,
                inPredictiveBack = inPredictive,
            )
            stack[target] = z
        }

        step(initial = "root", target = "A", isPop = false, inPredictive = false)
        step(initial = "A", target = "B", isPop = false, inPredictive = false)
        step(initial = "B", target = "C", isPop = false, inPredictive = false)
        // Now pop back through the chain: each hop must land strictly below the outgoing.
        step(initial = "C", target = "B", isPop = true, inPredictive = false)
        step(initial = "B", target = "A", isPop = false, inPredictive = true)
        step(initial = "A", target = "root", isPop = false, inPredictive = true)

        assertTrue(
            stack.getValue("root") < stack.getValue("A"),
            "root ($stack) must be below A during predictive back",
        )
        assertTrue(
            stack.getValue("A") < stack.getValue("B"),
            "A ($stack) must be below B during predictive back",
        )
        assertTrue(
            stack.getValue("B") < stack.getValue("C"),
            "B ($stack) must be below C after pop",
        )
    }

    @Test
    fun round_trip_via_deep_screen_keeps_outgoing_above_revealed() {
        // Exact bug scenario reported by the app: user goes ScanIntro → ScanBarcode →
        // IdentificationIntro, pops back to ScanBarcode, then predictive-backs to
        // ScanIntro. ScanBarcode (outgoing) must stay above ScanIntro (revealed).
        val z = mutableMapOf<String, Float>()

        fun step(initial: String, target: String, isPop: Boolean, inPredictive: Boolean) {
            val next = computeMonotonicZIndex(
                initialKey = initial,
                targetKey = target,
                initialZIndex = z.getOrPut(initial) { 0f },
                existingTargetZIndex = z[target],
                targetIsScene = false,
                isPop = isPop,
                inPredictiveBack = inPredictive,
            )
            z[target] = next
        }

        step("ScanIntro", "ScanBarcode", isPop = false, inPredictive = false)
        step("ScanBarcode", "IdentificationIntro", isPop = false, inPredictive = false)
        step("IdentificationIntro", "ScanBarcode", isPop = false, inPredictive = true)
        // Simulate cleanup after settle on ScanBarcode — the container prunes the
        // map to just the settled key. Without this, we'd still test the bug case,
        // but the composable includes this prune so the test should mirror it.
        z.keys.filter { it != "ScanBarcode" }.forEach(z::remove)
        step("ScanBarcode", "ScanIntro", isPop = false, inPredictive = true)

        assertTrue(
            z.getValue("ScanIntro") < z.getValue("ScanBarcode"),
            "ScanBarcode ($z) must draw above ScanIntro during predictive back",
        )
    }
}
