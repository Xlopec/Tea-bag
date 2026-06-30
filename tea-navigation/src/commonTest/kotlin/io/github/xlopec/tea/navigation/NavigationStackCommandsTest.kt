package io.github.xlopec.tea.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the command-generating overloads of the mutator ops. The command-free
 * versions live in [NavigationStackTest].
 */
class NavigationStackCommandsTest {

    private fun ids(vararg names: String): NavigationStack<TestEntry> = stack(*names)
    private fun List<TestEntry>.ids(): List<String> = map { it.id }

    @Test
    fun pop_with_onPop_collects_commands() {
        val (next, commands) = ids("a", "b").mutate<_, String> {
            pop { setOf("dispose-${it.id}") }
        }
        assertEquals(listOf("a"), next.value.ids())
        assertEquals(setOf("dispose-b"), commands)
    }

    @Test
    fun popUntil_with_onPop_collects_commands_for_each_pop() {
        val (next, commands) = ids("a", "b", "c", "d").mutate<_, String> {
            popUntil(predicate = { it.id == "b" }) { setOf("dispose-${it.id}") }
        }
        assertEquals(listOf("a", "b"), next.value.ids())
        assertEquals(setOf("dispose-d", "dispose-c"), commands)
    }

    @Test
    fun popTo_with_onPop_collects_commands_for_each_pop() {
        val (next, commands) = ids("a", "b", "c", "d").mutate<_, String> {
            popTo(id = "b") { setOf("dispose-${it.id}") }
        }
        assertEquals(listOf("a", "b"), next.value.ids())
        assertEquals(setOf("dispose-d", "dispose-c"), commands)
    }

    @Test
    fun replaceTop_records_commands_when_provided() {
        var removed: TestEntry? = null
        val (next, commands) = ids("a", "b").mutate<_, String> {
            removed = replaceTop(TestEntry("z"), setOf("loaded-z"))
        }
        assertEquals(TestEntry("b"), removed)
        assertEquals(listOf("a", "z"), next.value.ids())
        assertEquals(setOf("loaded-z"), commands)
    }

    @Test
    fun clearAndPush_records_commands_when_provided() {
        val (next, commands) = ids("a", "b").mutate<_, String> {
            clearAndPush(TestEntry("home"), setOf("init-home"))
        }
        assertEquals(listOf("home"), next.value.ids())
        assertEquals(setOf("init-home"), commands)
    }
}
