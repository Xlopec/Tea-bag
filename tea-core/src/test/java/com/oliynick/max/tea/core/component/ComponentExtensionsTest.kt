/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.oliynick.max.tea.core.component

import core.component.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentExtensionsTest {

    private val initialState = TodoState(Item("some1"), Item("some2"), Item("some3"))

    @Test
    fun `test single command extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)

        val (state, commands) = initialState.command(DoAddItem(Item("some4"), initialState.items))

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first)
    }

    @Test
    fun `test two commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)

        val (state, commands) = initialState.command(first, second)

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first, second)
    }

    @Test
    fun `test three commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)

        val (state, commands) = initialState.command(first, second, third)

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first, second, third)
    }

    @Test
    fun `test multiple commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)
        val fourth = DoAddItem(Item("some7"), initialState.items)

        val (state, commands) = initialState.command(first, second, third, fourth)

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first, second, third, fourth)
    }

    @Test
    fun `test no commands extension`() {
        val (state, commands) = initialState.noCommand()

        initialState shouldBe state
        commands.shouldBeEmpty()
    }

    @Test
    fun `test side effect`() = runBlockingTest {

        val messages = DoAddItem(Item("some"), emptyList()).sideEffect<Command, Updated> { }

        messages.shouldBeEmpty()
    }

    @Test
    fun `test when effect returns no command the result is empty set`() = runBlockingTest {
        val messages = DoAddItem(Item("some"), emptyList()).effect<Command, Updated> { null }

        messages.shouldBeEmpty()
    }

    @Test
    fun `test when effect returns command the result is set containing the messages`() =
        runBlockingTest {
            val item = Item("some")
            val expectedMessage = Updated(listOf(item))
            val messages = DoAddItem(item, emptyList()).effect { expectedMessage }

            messages.shouldContainExactlyInAnyOrder(expectedMessage)
        }

}
