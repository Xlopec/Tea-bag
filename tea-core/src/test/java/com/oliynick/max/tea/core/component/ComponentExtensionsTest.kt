/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.tea.core.component

import core.component.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.util.concurrent.Executors

typealias Transformer<E, R> = (E) -> Flow<R>
typealias IdentityTransformer<E> = Transformer<E, E>

@RunWith(JUnit4::class)
class ComponentExtensionsTest {

    private val initialState = TodoState(Item("some1"), Item("some2"), Item("some3"))
    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `test single command extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)

        val (state, commands) = initialState.command(DoAddItem(Item("some4"), initialState.items))

        expectThat(initialState).isEqualTo(state)
        expectThat(commands).containsExactlyInAnyOrder(first)
    }

    @Test
    fun `test two commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)

        val (state, commands) = initialState.command(first, second)

        expectThat(initialState).isEqualTo(state)
        expectThat(commands).containsExactlyInAnyOrder(first, second)
    }

    @Test
    fun `test three commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)

        val (state, commands) = initialState.command(first, second, third)

        expectThat(initialState).isEqualTo(state)
        expectThat(commands).containsExactlyInAnyOrder(first, second, third)
    }

    @Test
    fun `test multiple commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)
        val fourth = DoAddItem(Item("some7"), initialState.items)

        val (state, commands) = initialState.command(first, second, third, fourth)

        expectThat(initialState).isEqualTo(state)
        expectThat(commands).containsExactlyInAnyOrder(first, second, third, fourth)
    }

    @Test
    fun `test no commands extension`() {
        val (state, commands) = initialState.noCommand<TodoState, DoAddItem>()

        expectThat(initialState).isEqualTo(state)
        expectThat(commands).isEmpty()
    }

    @Test
    fun `test side effect`() = runBlockingTest {

        val messages = DoAddItem(Item("some"), emptyList()).sideEffect<Command, Updated> { }

        expectThat(messages).isEmpty()
    }

    @Test
    fun `test when effect returns no command the result is empty set`() = runBlockingTest {
        val messages = DoAddItem(Item("some"), emptyList()).effect<Command, Updated> { null }

        expectThat(messages).isEmpty()
    }

    @Test
    fun `test when effect returns command the result is set containing the messages`() =
        runBlockingTest {
            val item = Item("some")
            val expectedMessage = Updated(listOf(item))
            val messages = DoAddItem(item, emptyList()).effect { expectedMessage }


            expectThat(messages).containsExactlyInAnyOrder(expectedMessage)
        }

}
