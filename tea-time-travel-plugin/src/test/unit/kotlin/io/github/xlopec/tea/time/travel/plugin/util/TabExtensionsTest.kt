package io.github.xlopec.tea.time.travel.plugin.util

import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Test

class TabExtensionsTest {

    private companion object {
        private val TestTabContent = arrayOf(
            "a" to 1,
            "b" to 2,
            "c" to 3,
            "d" to 4,
        )
    }

    @Test
    fun `check ordering preserved for Debugger components`() {
        val ids = listOf(
            ComponentId("a"),
            ComponentId("b"),
            ComponentId("c"),
            ComponentId("d"),
            ComponentId("e"),
        )

        val mapping = persistentMapOf(*ids.map { ComponentDebugState(it) }.toTypedArray())
        assertEquals(ids, mapping.keys.toList())

        // add element
        val (k, v) = ComponentDebugState(TestComponentId)
        assertEquals(ids + k, mapping.put(k, v).keys.toList())

        // remove element
        assertEquals(ids.subList(1, ids.size), mapping.remove(ids.first()).keys.toList())
    }

    @Test
    fun `check next left tab is selected when closing a tab given there is tab to the left`() {
        val mapping = persistentMapOf(*TestTabContent)

        TestTabContent
            .map(Pair<String, *>::first).subList(1, TestTabContent.size)
            .forEachIndexed { index, k ->
                assertEquals(mapping[TestTabContent[index].first], mapping.nextSelectionForClosingTab(k))
            }
    }

    @Test
    fun `check next right tab is selected when closing tab 'a' given there is no tab to the left`() {
        assertEquals(
            TestTabContent[1].second,
            persistentMapOf(*TestTabContent).nextSelectionForClosingTab(TestTabContent[0].first)
        )
    }

    @Test
    fun `check same tab is selected when closing tab given there single element in a map`() {
        val (k, v) = TestTabContent.first()

        assertEquals(v, persistentMapOf(TestTabContent.first()).nextSelectionForClosingTab(k))
    }

    @Test
    fun `check exception is thrown when closing non-existing tab`() {
        assertFails { persistentMapOf(*TestTabContent).nextSelectionForClosingTab("") }
    }

    @Test
    fun `check exception is thrown when closing tab given mapping is empty`() {
        assertFails { persistentMapOf<String, Nothing>().nextSelectionForClosingTab("") }
    }
}
