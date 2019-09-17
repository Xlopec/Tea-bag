@file:Suppress("TestFunctionName")

package com.oliynick.max.elm.core.component

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executors

@RunWith(JUnit4::class)
class ComponentTest {

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val initial = TodoState()

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
    fun `test when subscribing the last state is propagated`() = runBlockingTest {
        val lastIndex = 10
        val states = generateHistoricalStates(lastIndex)
        val channel = Channel<AddItem>()

        for (s in states) {
            val i = s.items.lastOrNull() ?: continue

            launch { channel.send(AddItem(i)) }
        }

        val component = Component()

        val collectorJob = launch {
            component(channel.consumeAsFlow()).collectIndexed { index, value ->
                // we will gen lastIndex + 1 invocations since the very first
                // value will be redelivered
                if (index <= lastIndex) {
                    assertEquals(states[index], value)
                }
            }
        }


        channel.close()
        // stop listening to component changes
        collectorJob.cancelAndJoin()
    }

    @Test
    fun `test when subscribing multiple times the last emitted state is propagated`() = runBlockingTest {
        val component = Component()

        // first time

        assertEquals(initial, component.changes().first())
        assertEquals(initial, component.changes().first())

        // second time

        val item = Item("Something")

        val job = async { component(flowOf(AddItem(item))).collect() }

        val expectedSecondTime = TodoState(listOf(item))

        assertEquals(expectedSecondTime, component.changes().first())
        assertEquals(expectedSecondTime, component.changes().first())

        job.cancelAndJoin()
    }

    @Test
    fun `test when removing item from an empty list the list stays unchanged`() = runBlockingTest {
        val state = Component()(flowOf(RemoveItem(Item("some")))).first()

        assertEquals(state.items, emptyList<Item>())
    }

    @Test
    fun `test item addition ordering is correct`() = runBlockingTest {
        val item = Item("some")
        val states = Component()(flowOf(AddItem(item))).take(2).toCollection(ArrayList())

        assertEquals(emptyList<Item>(), states[0].items)
        assertEquals(listOf(item), states[1].items)
    }

    private fun Component() = component(initial, ::testResolver, ::testUpdate)
}

private suspend fun testResolver(cmd: Command): Set<Message> {
    return when (cmd) {
        is DoAddItem -> cmd.effect { Updated(from + item) }
        is DoRemoveItem -> cmd.effect { Updated(from - item) }
    }
}

private fun testUpdate(message: Message, state: TodoState): UpdateWith<TodoState, Command> {
    return when (message) {
        is Updated -> TodoState(message.items).noCommand()
        is AddItem -> state command DoAddItem(message.item, state.items)
        is RemoveItem -> state command DoRemoveItem(message.item, state.items)
    }
}

private fun generateHistoricalStates(lastIndex: Int): List<TodoState> {
    val states = mutableListOf<TodoState>()

    for (i in 0..lastIndex) {
        states += TodoState((0 until i).map { Item("some $it") })
    }

    return states
}