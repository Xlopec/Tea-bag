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

package core.component

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.time.travel.component
import core.misc.invokeCollecting
import core.scope.runBlockingInTestScope
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executors

@RunWith(JUnit4::class)
class ActorComponentTest {

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val initial = TodoState()

    @get:Rule
    val exceptionRule: ExpectedException = ExpectedException.none()

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
    fun `test when subscribing the last state is propagated`() = componentTest { component ->
        val states = generateHistoricalStates()
        val channel = Channel<AddItem>()

        for (s in states) {
            val i = s.items.lastOrNull() ?: continue

            launch { channel.send(AddItem(i)) }
        }

        val collectorJob = launch {
            component(channel.consumeAsFlow()).collectIndexed { index, value ->
                // we will gen lastIndex + 1 invocations since the very first
                // value will be redelivered
                if (index <= states.lastIndex) {
                    assertEquals(states[index], value)
                }
            }
        }

        channel.close()
        // stop listening to component changes
        collectorJob.cancel()
    }

    @Test
    fun `test when subscribing multiple times the last emitted state is propagated`() = componentTest { component ->
        // first time

        assertEquals("First check has failed", initial, component.changes().first())
        assertEquals("First check has failed", initial, component.changes().first())

        // second time

        val item = Item("Something")
        // mutate internal state by emitting the message
        launch { component(AddItem(item)).first() }.join()

        val expectedSecondTime = TodoState(listOf(item))

        assertEquals("Second check has failed", expectedSecondTime, component.changes().first())
        assertEquals("Second check has failed", expectedSecondTime, component.changes().first())
    }

    @Test
    fun `test when removing item from an empty list the list stays unchanged`() = componentTest { component ->
        val state = component(RemoveItem(Item("some"))).first()

        assertEquals(state.items, emptyList<Item>())
    }

    @Test
    fun `test when touching a component after canceling a scope results in exception`() = componentTest { component ->
        val item = Item("some")

        val states = component.invokeCollecting(AddItem(item), elems = 2)

        assertEquals(emptyList<Item>(), states[0].items)
        assertEquals(listOf(item), states[1].items)

        cancel()

        exceptionRule.expect(IllegalStateException::class.java)
        exceptionRule.expectMessage("Component was already disposed")

        component(AddItem(item)).first()
    }

    @Test
    fun `test item addition ordering is correct`() = componentTest { component ->
        val item = Item("some")

        val states = component.invokeCollecting(AddItem(item), elems = 2)

        assertEquals(emptyList<Item>(), states[0].items)
        assertEquals(listOf(item), states[1].items)
    }

    @Test
    fun `test when specifying initial command it gets processed`() =
        componentTest(DoAddItem(Item("some1"), emptyList())) { component ->
            // since command is processed asynchronously we should wait a little bit
            val state = component.changes().first { it.items.isNotEmpty() }

            assertEquals(listOf(Item("some1")), state.items)
        }

    private fun CoroutineScope.testComponent(vararg initialCommands: Command) =
        component(initial, ::testResolver, ::testUpdate, initialCommands = *initialCommands)

    /**
     * Creates test scope that emulates [scope][CoroutineScope] that can be [canceled][cancel] for test purposes
     */
    private fun componentTest(vararg initialCommands: Command, block: suspend CoroutineScope.(component: Component<Message, TodoState>) -> Unit) {
        runBlockingInTestScope { block(testComponent(*initialCommands)) }
    }

}

private fun generateHistoricalStates(size: Int = 10): List<TodoState> {
    val states = mutableListOf<TodoState>()

    for (i in 0 until size) {
        states += TodoState((0 until i).map { Item("some $it") })
    }

    return states
}