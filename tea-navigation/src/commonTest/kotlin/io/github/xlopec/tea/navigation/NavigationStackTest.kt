package io.github.xlopec.tea.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigationStackTest {

    private fun ids(vararg names: String): NavigationStack<TestEntry> = stack(*names)
    private fun List<TestEntry>.ids(): List<String> = map { it.id }

    @Test
    fun screen_returns_top_of_stack() {
        assertEquals(TestEntry("c"), ids("a", "b", "c").screen)
    }

    @Test
    fun previousScreen_returns_second_from_top() {
        assertEquals(TestEntry("b"), ids("a", "b", "c").previousScreen)
    }

    @Test
    fun previousScreen_returns_null_at_root() {
        assertNull(ids("only").previousScreen)
    }

    @Test
    fun push_appends_to_top() {
        val (next, commands) = ids("a").mutate<_, Nothing> { push(TestEntry("b")) }
        assertEquals(listOf("a", "b"), next.value.ids())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun pop_removes_top_and_returns_it() {
        var popped: TestEntry? = null
        val (next, _) = ids("a", "b").mutate<_, Nothing> { popped = pop() }
        assertEquals(TestEntry("b"), popped)
        assertEquals(listOf("a"), next.value.ids())
    }

    @Test
    fun popAll_removes_matching_entries_and_collects_commands() {
        val (next, commands) = ids("a", "b", "a", "c", "a").mutate<_, String> {
            popAll(predicate = { it.id == "a" }, onPop = { setOf("removed-${it.id}") })
        }
        assertEquals(listOf("b", "c"), next.value.ids())
        assertEquals(setOf("removed-a"), commands)
    }

    @Test
    fun replaceTop_swaps_top_and_returns_old() {
        var removed: TestEntry? = null
        val (next, _) = ids("a", "b", "c").mutate<_, Nothing> {
            removed = replaceTop(TestEntry("z"))
        }
        assertEquals(TestEntry("c"), removed)
        assertEquals(listOf("a", "b", "z"), next.value.ids())
    }

    @Test
    fun popUntil_removes_until_predicate_holds() {
        var removed: List<TestEntry> = emptyList()
        val (next, _) = ids("a", "b", "c", "d").mutate<_, Nothing> {
            removed = popUntil(predicate = { it.id == "b" })
        }
        assertEquals(listOf("a", "b"), next.value.ids())
        assertEquals(listOf("d", "c"), removed.ids())
    }

    @Test
    fun popUntil_stops_at_root_even_if_predicate_never_holds() {
        val (next, _) = ids("a", "b", "c").mutate<_, Nothing> {
            popUntil(predicate = { it.id == "missing" })
        }
        assertEquals(listOf("a"), next.value.ids())
    }

    @Test
    fun popUntil_is_noop_when_predicate_already_holds_at_top() {
        val (next, _) = ids("a", "b", "c").mutate<_, Nothing> {
            popUntil(predicate = { it.id == "c" })
        }
        assertEquals(listOf("a", "b", "c"), next.value.ids())
    }

    @Test
    fun popTo_pops_until_id_is_top() {
        val (next, _) = ids("a", "b", "c", "d").mutate<_, Nothing> { popTo("b") }
        assertEquals(listOf("a", "b"), next.value.ids())
    }

    @Test
    fun clearAndPush_resets_stack_to_single_entry() {
        var removed: List<TestEntry> = emptyList()
        val (next, _) = ids("a", "b", "c").mutate<_, Nothing> {
            removed = clearAndPush(TestEntry("home"))
        }
        assertEquals(listOf("home"), next.value.ids())
        assertEquals(listOf("a", "b", "c"), removed.ids())
    }

    @Test
    fun updateInstanceOf_updates_every_match() {
        val (next, commands) = ids("a", "b", "a").mutate<_, String> {
            updateInstanceOf<_, _, TestEntry> { entry ->
                TestEntry(entry.id.uppercase()) to setOf("touched-${entry.id}")
            }
        }
        assertEquals(listOf("A", "B", "A"), next.value.ids())
        assertEquals(setOf("touched-a", "touched-b"), commands)
    }

    @Test
    fun updateInstanceOfById_updates_only_the_matching_entry() {
        val (next, commands) = ids("a", "b", "c").mutate<_, String> {
            updateInstanceOfById<_, _, _, TestEntry>("b") {
                TestEntry("B!") to setOf("hit")
            }
        }
        assertEquals(listOf("a", "B!", "c"), next.value.ids())
        assertEquals(setOf("hit"), commands)
    }

    @Test
    fun toStackOrNull_returns_null_on_empty() {
        assertNull(emptyList<TestEntry>().toStackOrNull())
    }

    @Test
    fun toStackOrNull_wraps_non_empty_list() {
        val s = listOf(TestEntry("a"), TestEntry("b")).toStackOrNull()
        assertEquals(listOf("a", "b"), s!!.value.ids())
    }
}
