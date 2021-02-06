/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

typealias StringSnapshot = Snapshot<String, String, String>

@RunWith(JUnit4::class)
@OptIn(InternalCoroutinesApi::class)
class DebuggableComponentTest : BasicComponentTest(::ComponentFactory) {

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
