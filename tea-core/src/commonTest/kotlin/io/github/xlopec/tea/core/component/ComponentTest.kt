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

@file:Suppress("FunctionName", "TestFunctionName")

package io.github.xlopec.tea.core.component

import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.computeSnapshots
import io.github.xlopec.tea.core.misc.NoOpResolver
import io.github.xlopec.tea.core.misc.TestEnv
import io.github.xlopec.tea.core.misc.TestTimeoutMillis
import io.github.xlopec.tea.core.misc.noOpSink
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class ComponentTest : ComponentTestBase(::Component) {

    @Test
    fun `when upstream receives new input then previous downstream is canceled`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv(
                Initializer(""),
                NoOpResolver(),
                { message, state -> state command message }
            )

            val lastInitial = Initial("b", setOf('e'))

            val initialStates = listOf(
                Initial("", setOf('c')),
                Initial("a", setOf('d')),
                lastInitial
            )

            fun testInput(
                input: Initial<String, Char>,
            ) = input.commands.asFlow()
                .onStart {
                    if (input !== initialStates.last()) {
                        delay(Long.MAX_VALUE)
                    }
                }

            val actualStates = env.computeSnapshots(initialStates.asFlow(), ::noOpSink, ::testInput)
                .toList()

            val (state, commands) = lastInitial
            val expectedStates = initialStates + Regular(state, commands, state, commands.first())

            assertContentEquals(expectedStates, actualStates)
        }
}
