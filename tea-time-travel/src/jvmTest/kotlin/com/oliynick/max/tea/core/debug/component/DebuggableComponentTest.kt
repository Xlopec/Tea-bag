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

@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.component

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.invoke
import com.oliynick.max.tea.core.debug.gson.GsonNotifyComponentAttached
import com.oliynick.max.tea.core.debug.gson.GsonNotifyComponentSnapshot
import com.oliynick.max.tea.core.debug.misc.*
import com.oliynick.max.tea.core.debug.session.WebSocketSession
import core.component.BasicComponentTest
import core.scope.runBlockingInTestScope
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.matchers.numerics.shouldNotBeExactly
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.ConnectException

public typealias StringSnapshot = Snapshot<String, String, String>

@RunWith(JUnit4::class)
@OptIn(InternalCoroutinesApi::class)
internal class DebuggableComponentTest : BasicComponentTest(::ComponentFactory) {

    private companion object {

        fun ComponentFactory(
            @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
            env: Env<Char, String, Char>,
        ): Component<Char, String, Char> = Component(TestDebugEnv(env = env))
    }

    @Test
    fun `test debuggable component throws expected exception when it can't connect to a server`() =
        runBlockingInTestScope {

            val env = TestEnv()

            val component = Component(
                TestDebugEnv(
                    env = env,
                    serverSettings = TestServerSettings(sessionBuilder = ::WebSocketSession)
                )
            )

            val job = env.scope.launch { component("a").collect() }

            job.join()
            job.isCancelled.shouldBeTrue()

            val th = job.getCancellationException().cause

            withClue("Cancellation cause $th") {
                th.shouldNotBeNull()
                th.shouldBeTypeOf<ConnectException>()
            }

            env.scope.isActive.shouldBeFalse()
        }

    @Test
    fun `test debuggable component sends the same sequence of events as the original component`() =
        runBlockingInTestScope {

            val testSession = TestDebugSession<String, String>()
            val component = Component(
                TestDebugEnv(
                    env = TestEnv(),
                    serverSettings = TestServerSettings(
                        sessionBuilder = { _, block -> testSession.apply { block() } }
                    )
                )
            )
            val messages = arrayOf("a", "b", "c")
            val actual = component(*messages).take(messages.size + 1)
                .toCollection(ArrayList(messages.size + 1))

            actual shouldBe listOf(
                Initial("", emptySet()),
                Regular("a", emptySet(), "", "a"),
                Regular("b", emptySet(), "a", "b"),
                Regular("c", emptySet(), "b", "c")
            )

            testSession.packets.forEachIndexed { index, elem ->

                elem.componentId shouldBe TestComponentId

                when (val payload = elem.payload) {
                    is GsonNotifyComponentSnapshot -> {
                        index shouldNotBeExactly 0
                        fromJson(payload.message) shouldBe messages[index - 1]
                        fromJson(payload.newState) shouldBe messages[index - 1]
                    }

                    is GsonNotifyComponentAttached -> {
                        index shouldBeExactly 0
                        fromJson(payload.state) shouldBe ""
                    }
                }
            }
        }

    @Test
    fun `test debuggable component processes server snapshots properly`() = runBlockingInTestScope {

        val testSession = TestDebugSession<String, String>(states = flowOf("a"))
        val component = Component(
            TestDebugEnv(
                env = TestEnv(
                    initializer = { delay(Long.MAX_VALUE); error("shouldn't get here") }
                ),
                serverSettings = TestServerSettings(
                    sessionBuilder = { _, block -> testSession.apply { block() } }
                )
            )
        )

        val expected = listOf<StringSnapshot>(
            Initial("a", emptySet()),
            Regular("b", emptySet(), "a", "b")
        )

        val actual = component("b")
            .take(2)
            .toCollection(ArrayList(2))

        actual shouldContainExactly expected
    }

    @Test
    @Ignore("race, can't make any assumptions regarding processing order")
    fun `test debuggable component processes server messages properly`() = runBlockingInTestScope {

        val serverMessages = Channel<String>()
        val testSession =
            TestDebugSession<String, String>(messages = serverMessages.consumeAsFlow())
        val component = Component(
            TestDebugEnv(
                env = TestEnv(),
                serverSettings = TestServerSettings(
                    sessionBuilder = { _, block -> testSession.apply { block() } }
                )
            )
        )

        val expected = listOf<StringSnapshot>(
            Initial("", emptySet()),
            Regular("a", emptySet(), "", "a"),
            Regular("b", emptySet(), "a", "b"),
            Regular("c", emptySet(), "b", "c"),
            Regular("d", emptySet(), "c", "d")
        )

        val actual = component(flowOf("a").onCompletion { serverMessages.send("b", "c", "d") })
            .take(expected.size)
            .toCollection(ArrayList(expected.size))

        actual shouldContainExactly expected
    }

}

private fun fromJson(
    tree: JsonElement,
) = TestSerializer.fromJsonTree(tree, String::class.java)

private suspend fun <E> Channel<E>.send(
    vararg e: E,
) = e.forEach { elem -> send(elem) }
