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

package io.github.xlopec.tea.time.travel.component

import com.google.gson.JsonElement
import io.github.xlopec.tea.core.Env
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.component.ComponentTestBase
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.misc.ThrowingResolver
import io.github.xlopec.tea.core.misc.runTestCancellingChildren
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.gson.GsonNotifyComponentAttached
import io.github.xlopec.tea.time.travel.gson.GsonNotifyComponentSnapshot
import io.github.xlopec.tea.time.travel.misc.TestComponentId
import io.github.xlopec.tea.time.travel.misc.TestDebugEnv
import io.github.xlopec.tea.time.travel.misc.TestDebugSession
import io.github.xlopec.tea.time.travel.misc.TestSerializer
import io.github.xlopec.tea.time.travel.misc.TestSettings
import io.github.xlopec.tea.time.travel.protocol.JsonSerializer
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.junit.Test

class DebuggableComponentTest : ComponentTestBase({ env -> Component(TestDebugEnv(env)) }) {

    @Test
    fun `test debuggable component throws expected exception when it can't connect to a server`() {
        runTestCancellingChildren {
            val exceptions = mutableListOf<Throwable>()
            val env = Env<Char, String, Char>(
                Initializer(""),
                ThrowingResolver(),
                { m, _ -> m.toString().noCommand() },
                CoroutineScope(Job() + CoroutineExceptionHandler { _, th -> exceptions += th })
            )

            runCatching {

                val component = Component(
                    TestDebugEnv(
                        env = env,
                        settings = TestSettings(sessionFactory = { _, _ -> throw ComponentException("Couldn't connect to debug server") })
                    )
                )
                val job = env.scope.launch { component(flowOf('a')).collect() }

                job.join()
            }

            assertFalse(env.scope.isActive)
            assertTrue(exceptions.isNotEmpty())
            assertIs<ComponentException>(exceptions.first())
        }
    }

    @Test
    fun `test debuggable component sends the same sequence of events as the original component`() =
        runTestCancellingChildren {

            val testSession = TestDebugSession<Char, String>()
            val component = Component(
                TestDebugEnv(
                    env = Env<Char, String, Char>(
                        Initializer(""),
                        ThrowingResolver(),
                        { m, _ -> m.toString().noCommand() },
                        this
                    ),
                    settings = TestSettings(
                        sessionFactory = { _, block -> testSession.apply { block() } }
                    )
                )
            )
            val messages = arrayOf('a', 'b', 'c')
            val actual = component(*messages).take(messages.size + 1)
                .toCollection(ArrayList(messages.size + 1))

            assertEquals(
                listOf(
                    Initial("", emptySet()),
                    Regular("a", emptySet(), "", 'a'),
                    Regular("b", emptySet(), "a", 'b'),
                    Regular("c", emptySet(), "b", 'c')
                ), actual
            )

            assertTrue(testSession.packets.isNotEmpty())

            testSession.packets.forEachIndexed { index, elem ->

                assertEquals(TestComponentId, elem.componentId)

                when (val payload = elem.payload) {
                    is GsonNotifyComponentSnapshot -> {
                        assertNotEquals(0, index)
                        assertEquals(messages[index - 1], TestSerializer.fromJson(payload.message))
                        assertEquals(messages[index - 1], TestSerializer.fromJson(payload.newState))
                    }

                    is GsonNotifyComponentAttached -> {
                        assertEquals(0, index)
                        assertEquals("", TestSerializer.fromJson(payload.state))
                    }
                }
            }
        }
}

private inline fun <reified T : Any> JsonSerializer<JsonElement>.fromJson(
    tree: JsonElement,
) = fromJsonTree(tree, T::class)
