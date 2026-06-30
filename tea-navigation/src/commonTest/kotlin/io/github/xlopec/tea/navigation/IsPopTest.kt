package io.github.xlopec.tea.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsPopTest {

    @Test
    fun pop_removing_top_entry_is_detected() {
        assertTrue(isPop(stack("a", "b", "c"), stack("a", "b")))
    }

    @Test
    fun pop_removing_multiple_entries_is_detected() {
        assertTrue(isPop(stack("a", "b", "c", "d"), stack("a")))
    }

    @Test
    fun push_is_not_a_pop() {
        assertFalse(isPop(stack("a"), stack("a", "b")))
    }

    @Test
    fun equal_stacks_are_not_a_pop() {
        assertFalse(isPop(stack("a", "b"), stack("a", "b")))
    }

    @Test
    fun replacing_the_root_is_not_a_pop() {
        assertFalse(isPop(stack("a", "b"), stack("z", "b")))
    }

    @Test
    fun replacing_middle_entry_keeping_same_size_is_not_a_pop() {
        assertFalse(isPop(stack("a", "b", "c"), stack("a", "x", "c")))
    }

    @Test
    fun pop_then_push_to_different_entry_is_not_a_pop() {
        assertFalse(isPop(stack("a", "b"), stack("a", "c")))
    }
}
