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

import app.cash.turbine.test
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.computeSnapshots
import io.github.xlopec.tea.core.misc.TestEnv
import io.github.xlopec.tea.core.misc.TestTimeoutMillis
import io.github.xlopec.tea.core.misc.expectCompletionAndCancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class ComponentTest : ComponentTestBase(::Component) {

    @Test
    fun `when upstream receives new input then previous downstream is canceled`() = runTest(timeout = TestTimeoutMillis.milliseconds) {
        val env = TestEnv(
            initializer = Initializer(""),
            resolver = { _ -> },
            updater = { message: Char, state -> state command message },
            scope = this
        )

        val lastInitial = Initial(currentState = "b", commands = setOf('e'))

        val initialStates = listOf(
            Initial(currentState = "", commands = setOf('c')),
            Initial(currentState = "a", commands = setOf('d')),
            lastInitial
        )

        fun testInput(
            input: Initial<String, Char>,
        ): Flow<Char> = input.commands.asFlow()
            .onStart {
                if (input !== initialStates.last()) {
                    delay(Long.MAX_VALUE)
                }
            }

        env.computeSnapshots(initialSnapshots = initialStates.asFlow(), input = testInput(lastInitial)).test {
            val (state, commands) = lastInitial
            val expectedStates = initialStates + Regular(currentState = state, commands = commands, previousState = state, message = commands.first())

            expectedStates.forEach { expectedState ->
                assertEquals(expectedState, awaitItem())
            }
            expectCompletionAndCancel()
        }
    }
}
