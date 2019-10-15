/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.oliynick.max.elm.core.component

import core.misc.invokeCollecting
import core.scope.runBlockingInTestScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import strikt.api.expectThat
import strikt.assertions.containsExactly
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
    fun `test when effect returns command the result is set containing the messages`() = runBlockingTest {
        val item = Item("some")
        val expectedMessage = Updated(listOf(item))
        val messages = DoAddItem(item, emptyList()).effect { expectedMessage }


        expectThat(messages).containsExactlyInAnyOrder(expectedMessage)
    }

    @Test
    fun `test changes extension uses an empty flow as param`() = runBlockingTest {
        expectThat(identityComponent<Unit>().changes()).isEqualTo(emptyFlow())
    }

    @Test
    fun `test when supplying the identity component with items it returns the same ordered sequence of items`() = runBlockingTest {
        expectThat(identityComponent<String>().invokeCollecting("a", "b", "c")).containsExactly("a", "b", "c")
    }

    @Test
    fun `test when supplying the identity component with single item it returns the same item`() = runBlockingTest {
        expectThat(identityComponent<Int>()(1).first()).isEqualTo(1)
    }

    @Test
    fun `test component binding`() = runBlockingInTestScope {
        val supplier = component("", ::throwingResolver, ::messageAsStateUpdate)
        val (transformer, sink) = spyingIdentityTransformer<String>()

        bind(supplier, identityComponent(), transformer)

        supplier("a", "b").first()

        expectThat(listOf("", "a", "b")).containsExactly(sink)
    }

    @Test
    fun `test interceptors composition`() = runBlockingInTestScope {
        val (interceptor1, sink1) = spyingInterceptor()
        val (interceptor2, sink2) = spyingInterceptor()

        component("", ::throwingResolver, { m, _ -> m.noCommand() }, interceptor1 with interceptor2)
            .also { component -> /* modify state */ component("a", "b").first() }

        val expected = listOf(InterceptData("a", "", "a", emptySet()),
                              InterceptData("b", "a", "b", emptySet()))

        expectThat(expected).containsExactly(sink1)
        expectThat(expected).containsExactly(sink2)
    }

}

private fun <E> spyingIdentityTransformer(): Pair<IdentityTransformer<E>, List<E>> {
    val sink = mutableListOf<E>()

    return { input: E -> sink += input; flowOf(input) } to sink
}

private fun <E> identityComponent(): Component<E, E> = { it }

private fun spyingInterceptor(): Pair<Interceptor<String, String, String>, List<InterceptData>> {
    val sink = mutableListOf<InterceptData>()

    val interceptor: Interceptor<String, String, String> = { msg, old, new, cmds -> sink += InterceptData(msg, old, new, cmds) }

    return interceptor to sink
}

