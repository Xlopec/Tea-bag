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

import io.github.xlopec.reader.app.feature.navigation.Tab
import io.github.xlopec.tea.data.RandomUUID
import io.github.xlopec.tea.navigation.mutate
import io.github.xlopec.tea.navigation.toStackOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationExtensionsTest {

    @Test
    fun middleNavigationGroupFloatsCorrectly() {
        val group1 = listOf(*Group(Tab.Feed))
        val group2 = listOf(*Group(Tab.Settings))
        val group3 = listOf(*Group(Tab.Trending))

        val initialStack = (group1 + group2 + group3).toStackOrNull()!!
        val (actualNavigationStack, commands) = initialStack.mutate<_, _, Nothing> {
            switchToTab(Tab.Settings, ::testScreenBelongsToTab)
        }
        val expectedNavigationStack = (group1 + group3 + group2).toStackOrNull()!!

        assertTrue(commands.isEmpty())
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
        val group1 = listOf(*Group(Tab.Feed))
        val group2 = listOf(*Group(Tab.Settings))
        val group3 = listOf(*Group(Tab.Trending))

        val initialStack = (group1 + group2 + group3).toStackOrNull()!!
        val (actualNavigationStack, commands) = initialStack.mutate<_, _, Nothing> {
            switchToTab(Tab.Trending, ::testScreenBelongsToTab)
        }

        assertTrue(commands.isEmpty())
        assertEquals(initialStack, actualNavigationStack)
    }
}

private data class TestNestedScreen(
    override val tab: Tab,
    override val id: ScreenId,
) : TabScreen

private data class TestRootScreen(
    override val tab: Tab,
) : TabScreen {
    override val id: ScreenId = tab.id
}

private fun Group(
    tab: Tab,
    nestedItemsCount: UInt = 1U
) = arrayOf(
    // group root
    TestRootScreen(tab),
    // group
    *GenerateNestedScreens(tab, nestedItemsCount).toTypedArray(),
)

private fun GenerateNestedScreens(
    tab: Tab,
    times: UInt
) = (0U until times).map { TestNestedScreen(tab, RandomUUID()) }

private fun testScreenBelongsToTab(
    screen: TabScreen,
    tab: Tab,
) = (screen as? TabScreen)?.tab == tab
