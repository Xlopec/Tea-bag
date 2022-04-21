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

package io.github.xlopec.tea.core.component

import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.core.noCommand
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

internal class ComponentExtensionsTest {

    private val initialState = TodoState(Item("some1"), Item("some2"), Item("some3"))

    @Test
    fun `test single command extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)

        val (state, commands) = initialState.command(DoAddItem(Item("some4"), initialState.items))

        assertEquals(state, initialState)
        assertContains(commands, first)
    }

    @Test
    fun `test two commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)

        val (state, commands) = initialState.command(first, second)

        assertEquals(initialState, state)
        assertContains(commands, first)
        assertContains(commands, second)
    }

    @Test
    fun `test three commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)

        val (state, commands) = initialState.command(first, second, third)

        assertEquals(initialState, state)
        assertContains(commands, first)
        assertContains(commands, second)
        assertContains(commands, third)
    }

    @Test
    fun `test multiple commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)
        val fourth = DoAddItem(Item("some7"), initialState.items)

        val (state, commands) = initialState.command(first, second, third, fourth)

        assertEquals(initialState, state)
        assertContains(commands, first)
        assertContains(commands, second)
        assertContains(commands, third)
        assertContains(commands, fourth)
    }

    @Test
    fun `test no commands extension`() {
        val (state, commands) = initialState.noCommand()

        assertEquals(initialState, state)
        assertTrue("commands should be empty", commands::isEmpty)
    }

   /* @Test
    fun `test side effect`() = runTest {

        val messages = DoAddItem(Item("some"), emptyList()).sideEffect<Command, Updated> { }

        assertTrue("messages should be empty", messages::isEmpty)
    }*/

    @Test
    fun `test when effect returns no command the result is empty set`() = runTest {
        val messages = DoAddItem(Item("some"), emptyList()).effect<Command, Updated> { null }

        assertTrue("messages should be empty", messages::isEmpty)
    }

    @Test
    fun `test when effect returns command the result is set containing the messages`() =
        runTest {
            val item = Item("some")
            val expectedMessage = Updated(listOf(item))
            val messages = DoAddItem(item, emptyList()).effect { expectedMessage }

            assertContains(messages, expectedMessage)
        }
}
