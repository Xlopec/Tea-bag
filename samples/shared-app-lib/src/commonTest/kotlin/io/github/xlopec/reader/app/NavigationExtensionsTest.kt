/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("TestFunctionName")

package io.github.xlopec.reader.app

import io.github.xlopec.reader.app.feature.navigation.floatGroup
import io.github.xlopec.tea.data.RandomUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlinx.collections.immutable.persistentListOf

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