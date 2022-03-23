@file:Suppress("TestFunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.entities.shared.RandomUUID
import com.oliynick.max.reader.app.feature.navigation.floatGroup
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class NavigationExtensionsTest {

    @Test
    fun middleNavigationGroupFloatsCorrectly() {

        val group2Id = RandomUUID()

        val group1 = arrayOf(*Group(RandomUUID()))
        val group2 = arrayOf(*Group(group2Id))
        val group3 = arrayOf(*Group(RandomUUID()))

        val initialStack = persistentListOf(*group1, *group2, *group3)
        val actualNavigationStack =
            initialStack.floatGroup(initialStack.indexOfFirst { it.id == group2Id }, group2Id)
        val expectedNavigationStack = persistentListOf(*group2, *group1, *group3)

        assertEquals(
            expectedNavigationStack,
            actualNavigationStack,
            """
                Middle group didn't become topmost group, 
                initial $initialStack
                actual $actualNavigationStack
                expected $expectedNavigationStack
            """.trimIndent()
        )
    }

    @Test
    fun unmodifiedIfAlreadyOnTheTop() {
        val group1Id = RandomUUID()

        val group1 = arrayOf(*Group(group1Id))
        val group2 = arrayOf(*Group(RandomUUID()))
        val group3 = arrayOf(*Group(RandomUUID()))

        val initialStack = persistentListOf(*group1, *group2, *group3)
        val actualNavigationStack =
            initialStack.floatGroup(initialStack.indexOfFirst { it.id == group1Id }, group1Id)

        assertSame(initialStack, actualNavigationStack)
    }

    @Test
    fun failsForIncorrectGroupIndex() {
        assertFailsWith(IllegalArgumentException::class) {
            persistentListOf<ScreenState>().floatGroup(-1, RandomUUID())
        }
    }

}

private data class TestNestedScreen(
    override val tabId: ScreenId,
    override val id: ScreenId = RandomUUID()
) : NestedScreen

private data class TestRootScreen(
    override val id: ScreenId
) : TabScreen

private fun Group(
    groupId: ScreenId,
    nestedItemsCount: UInt = 1U
) = arrayOf(
    // group
    *GenerateNestedScreens(groupId, nestedItemsCount).toTypedArray(),
    // group root
    TestRootScreen(groupId),
)

private fun GenerateNestedScreens(
    groupId: ScreenId,
    times: UInt
) = (0U until times).map { TestNestedScreen(groupId) }