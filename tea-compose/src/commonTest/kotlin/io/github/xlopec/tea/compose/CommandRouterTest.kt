/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

package io.github.xlopec.tea.compose

import io.github.xlopec.tea.core.ExperimentalTeaApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTeaApi::class)
class CommandRouterTest {

    private data class Cmd(val key: String?, val payload: String)

    private fun router() = CommandRouter<String, Cmd> { it.key }

    @Test
    fun `when command is dispatched then it is delivered to the consumer of its key`() = runTest {
        val router = router()
        val received = CompletableDeferred<Cmd>()
        val consumer = launch { router.consume("A") { received.complete(it) } }

        router.dispatch(liveKeys = setOf("A"), commands = listOf(Cmd("A", "hello")))

        assertEquals(Cmd("A", "hello"), received.await())
        consumer.cancel()
    }

    @Test
    fun `when consumer attaches after dispatch then it still receives buffered commands`() = runTest {
        val router = router()

        router.dispatch(liveKeys = setOf("A"), commands = listOf(Cmd("A", "early")))

        val received = CompletableDeferred<Cmd>()
        val consumer = launch { router.consume("A") { received.complete(it) } }

        assertEquals(Cmd("A", "early"), received.await())
        consumer.cancel()
    }

    @Test
    fun `when command targets one key then consumer of another key does not receive it`() = runTest {
        val router = router()
        val receivedByB = mutableListOf<Cmd>()
        val consumerA = launch { router.consume("A") { /* drain */ } }
        val consumerB = launch { router.consume("B") { receivedByB += it } }

        router.dispatch(liveKeys = setOf("A", "B"), commands = listOf(Cmd("A", "for-A")))
        testScheduler.advanceUntilIdle()

        assertTrue(receivedByB.isEmpty(), "consumer of B should not receive A's command, got $receivedByB")
        consumerA.cancel()
        consumerB.cancel()
    }

    @Test
    fun `when command key is absent from liveKeys then the command is dropped`() = runTest {
        val router = router()
        val received = mutableListOf<Cmd>()
        val consumer = launch { router.consume("X") { received += it } }

        router.dispatch(liveKeys = emptySet(), commands = listOf(Cmd("X", "dropped")))
        testScheduler.advanceUntilIdle()

        assertTrue(received.isEmpty(), "expected no commands, got $received")
        consumer.cancel()
    }

    @Test
    fun `when command has null key then it is routed to the default mailbox`() = runTest {
        val router = router()
        val received = CompletableDeferred<Cmd>()
        val consumer = launch { router.consume(key = null) { received.complete(it) } }

        router.dispatch(liveKeys = emptySet(), commands = listOf(Cmd(null, "app-wide")))

        assertEquals(Cmd(null, "app-wide"), received.await())
        consumer.cancel()
    }

    @Test
    fun `when key leaves liveKeys then its consumer terminates`() = runTest {
        val router = router()
        val received = CompletableDeferred<Cmd>()
        val terminated = CompletableDeferred<Unit>()
        val consumer = launch {
            router.consume("A") { received.complete(it) }
            terminated.complete(Unit)
        }

        // Warm A's mailbox with a command, then wait until the consumer has actually
        // consumed it — this guarantees the consumer is parked on the same channel
        // instance that the next dispatch will close.
        router.dispatch(liveKeys = setOf("A"), commands = listOf(Cmd("A", "first")))
        received.await()

        router.dispatch(liveKeys = emptySet(), commands = emptyList())

        terminated.await()
        consumer.join()
    }

    @Test
    fun `when liveKeys is empty then the default mailbox is not closed`() = runTest {
        val router = router()
        val received = CompletableDeferred<Cmd>()
        val consumer = launch { router.consume(key = null) { received.complete(it) } }

        router.dispatch(liveKeys = emptySet(), commands = emptyList())
        router.dispatch(liveKeys = emptySet(), commands = listOf(Cmd(null, "still-alive")))

        assertEquals(Cmd(null, "still-alive"), received.await())
        consumer.cancel()
    }

    @Test
    fun `when perChannelCapacity is UNLIMITED then dispatch buffers many commands without a consumer`() = runTest {
        // Well above Channel.BUFFERED's default (64) — dispatch would suspend with the default
        // capacity since no consumer is attached, so completing this test proves the override.
        val total = 200
        val router = CommandRouter<String, Cmd>(perChannelCapacity = Channel.UNLIMITED) { it.key }

        router.dispatch(liveKeys = setOf("A"), commands = List(total) { Cmd("A", "msg-$it") })

        val allReceived = CompletableDeferred<Unit>()
        val received = mutableListOf<Cmd>()
        val consumer = launch {
            router.consume("A") {
                received += it
                if (received.size == total) allReceived.complete(Unit)
            }
        }
        allReceived.await()

        assertEquals(total, received.size)
        consumer.cancel()
    }
}
