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
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.Resolver
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.Updater
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.misc.ComponentException
import io.github.xlopec.tea.core.misc.ForeverWaitingResolver
import io.github.xlopec.tea.core.misc.TestEnv
import io.github.xlopec.tea.core.misc.TestTimeoutMillis
import io.github.xlopec.tea.core.misc.currentThreadName
import io.github.xlopec.tea.core.misc.runTestCancellingChildren
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.core.toComponentFlow
import io.github.xlopec.tea.core.with
import kotlin.Long.Companion.MAX_VALUE
import kotlin.math.abs
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield

class ComponentTest {

    @Test
    fun `when subscriber disconnects then component initializer is re-invoked`() =
        runTestCancellingChildren {

            var counter = 0
            val initial = Initial<String, Char>("", setOf())
            val env = TestEnv(
                { counter++; initial },
                NoOpResolver(),
                { m, str -> (str + m).command(m) },
            )

            val component = Component(env)

            component.collectRanged('a'..'f')
            component.collectRanged('g'..'k')
            // each time new subscriber attaches to a component
            // with no subscribers initializer should be invoked
            assertEquals(2, counter, "Counter should be equal 2")
        }

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
                        delay(MAX_VALUE)
                    }
                }

            val actualStates = env.toComponentFlow(initialStates.asFlow(), ::noOpSink, ::testInput)
                .toList()

            val (state, commands) = lastInitial
            val expectedStates = initialStates + Regular(state, commands, state, commands.first())

            assertContentEquals(expectedStates, actualStates)
        }

    @Test
    fun `when component receives input then it emits correct sequence of snapshots`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ThrowingResolver(),
                { m, _ -> m.toString().noCommand() }
            )

            val messages = arrayOf('a', 'b', 'c')
            val actualSnapshots = Component(env)(*messages).take(messages.size + 1).toList()
            val expectedSnapshots = listOf<Snapshot<Char, String, Char>>(
                Initial("", setOf()),
                Regular("a", setOf(), "", 'a'),
                Regular("b", setOf(), "a", 'b'),
                Regular("c", setOf(), "b", 'c')
            )

            assertContentEquals(expectedSnapshots, actualSnapshots)
        }

    @Test
    fun `when component receives input given recursive calculations then it emits correct sequence of snapshots`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { ch, ctx ->
                    // only message 'b' should be consumed
                    ctx effects { if (ch == 'a') ('b'..'d').toSet() else setOf() }
                },
                { m, str -> (str + m).command(m) }
            )

            val actualSnapshots = Component(env)('a').take(3).toList()
            val expectedSnapshots = listOf(
                Initial("", setOf()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )

            assertContentEquals(expectedSnapshots, actualSnapshots)
        }

    @Test
    fun `when attaching interceptor to component then original sequence of snapshots pipes through it`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { c, ctx -> ctx effects { setOf(c) } },
                { m, _ -> m.toString().noCommand() },
            )

            val sink = mutableListOf<Snapshot<Char, String, Char>>()
            val component = Component(env) with sink::add
            val messages = arrayOf('a', 'b', 'c')
            val snapshots =
                component(*messages).take(messages.size + 1).toList()

            assertContentEquals(snapshots, sink)
        }

    @Test
    fun `when component has multiple consumers then snapshots are shared among them`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { ch, ctx ->
                    ctx effects {
                        if (ch == 'a') setOf(
                            ch + 1,// only this message should be consumed
                            ch + 2,
                            ch + 3
                        ) else setOf()
                    }
                },
                { m, str -> (str + m).command(m) }
            )

            val take = 3
            val component = Component(env)
            val snapshots2Deferred = async { component(emptyFlow()).take(take).toList() }
            val snapshots1Deferred = async { component('a').take(take).toList() }

            val expectedSnapshots = listOf(
                Initial("", setOf()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )

            val actualSnapshots1 = snapshots1Deferred.await()
            val actualSnapshots2 = snapshots2Deferred.await()

            assertContentEquals(
                expectedSnapshots, actualSnapshots1, """
            snapshots1: $actualSnapshots1
            snapshots2: $actualSnapshots2
            expected: $expectedSnapshots
            """.trimIndent()
            )
        }

    @Test
    fun `when component has multiple consumers then component is initialized only once`() =
        runTestCancellingChildren {

            var invocations = 0
            val env = TestEnv<Char, String, Char>(
                { invocations++; yield(); Initial("bar", setOf()) },
                ThrowingResolver(),
                { _, s -> s.noCommand() },
                // SharingStarted.Lazily since in case of default option replay
                // cache will be disposed immediately causing test to fail
                shareOptions = ShareOptions(Lazily, 1U)
            )

            val component = Component(env)
            // Ensure component builder won't invoke initializer before first consumer arrives
            assertEquals(0, invocations)

            repeat(1_000) { launch { component('a').first() } }

            advanceUntilIdle()
            assertEquals(1, invocations)
        }

    /**
     * Ignored due to the [following issue](https://youtrack.jetbrains.com/issue/KT-47195)
     */
    @Test
    @Ignore
    fun `test component's job gets canceled properly`() =
        runTestCancellingChildren {

            val resolver = ForeverWaitingResolver<Char, Char>()
            val env = TestEnv(
                Initializer(""),
                resolver,
                { message, state -> state command message }
            )

            val messages = 'a'..'z'

            Component(env)(messages).take(messages.size + 1 /*plus initial snapshot*/).collect()

            val canceled = resolver.messages
                .consumeAsFlow()
                .take(messages.size)
                .toList()

            assertEquals(messages.toList(), canceled)
        }

    @Test
    fun `when component has multiple consumers then it can serve multiple message sources`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ThrowingResolver(),
                { m, _ -> m.toString().noCommand() }
            )

            val range = 'a'..'h'
            val component = Component(env)

            val chan1 = Channel<Char>()
            val chan2 = Channel<Char>()

            val snapshots2Deferred = async {
                component(chan2.consumeAsFlow())
                    .take(1 + range.count())
                    .toList()
            }

            val snapshots1Deferred = async {
                component(chan1.consumeAsFlow())
                    .take(1 + range.count())
                    .toList()
            }

            range.forEachIndexed { index, ch ->
                if (index % 2 == 0) {
                    chan1.send(ch)
                } else {
                    chan2.send(ch)
                }
                // forces enqueued coroutines to run
                runCurrent()
            }

            val expectedSnapshots: List<Snapshot<Char, String, Char>> =
                listOf(
                    Initial(
                        "",
                        setOf<Char>()
                    )
                ) + range.mapIndexed { index, ch ->
                    Regular(
                        ch.toString(),
                        setOf(),
                        if (index == 0) "" else ch.dec().toString(),
                        ch
                    )
                }

            val actualSnapshots1 = snapshots1Deferred.await()
            val actualSnapshots2 = snapshots2Deferred.await()
            // performance gain is miserable for this case
            @Suppress("ConvertArgumentToSet")
            assertContentEquals(
                expectedSnapshots, actualSnapshots1, """
            snapshots1: $actualSnapshots1
            snapshots2: $actualSnapshots2
            expected: $expectedSnapshots
            diff: ${expectedSnapshots - actualSnapshots1}
            """.trimIndent()
            )

            // performance gain is miserable for this case
            @Suppress("ConvertArgumentToSet")
            assertContentEquals(
                expectedSnapshots, actualSnapshots2, """
            snapshots1: $actualSnapshots1
            snapshots2: $actualSnapshots2
            expected: $expectedSnapshots
            diff: ${expectedSnapshots - actualSnapshots2}
            """.trimIndent()
            )
        }

    @Test
    fun `when collecting component given updater throws exception then it is handled by coroutine scope`() {
        val scope = TestScope(UnconfinedTestDispatcher(name = "Failing host scope"))

        val component = Component(
            Initializer("", "a"),
            ThrowingResolver<String>(),
            { m, s -> throw ComponentException("message=$m, state=$s") },
            scope
        )

        val job = scope.launch { component("").collect() }

        assertTrue(job.isCancelled)

        val th = job.getCancellationException().cause

        assertTrue("Cancellation cause $th") { th is ComponentException }
        assertTrue(!scope.isActive)
    }

    @Test
    fun `when collecting component given initializer throws exception then it is handled by coroutine scope`() {

        val scope = TestScope(UnconfinedTestDispatcher(name = "Failing host scope"))

        val expectedException = RuntimeException("hello")
        val component = Component(
            ThrowingInitializer(expectedException),
            ThrowingResolver<String>(),
            { _, s -> s },
            scope
        )

        val job = scope.launch { component("").collect() }

        assertTrue(job.isCancelled)

        val th = job.getCancellationException().cause

        assertTrue("Cancellation cause $th") {
            th is RuntimeException && th.message == expectedException.message
        }
        assertTrue(!scope.isActive)
    }

    @Test
    fun `when collecting component with specific dispatcher then updater runs on this dispatcher`() =
        runTestCancellingChildren {
            // All test schedulers use 'Test worker' as prefix, so to workaround this issue we use
            // custom dispatcher with different thread naming strategy
            val mainThreadNamePrefix = async { currentThreadName() }
            val env = CoroutineScope(Default).TestEnv<Char, String, Char>(
                Initializer(""),
                ThrowingResolver(),
                CheckingUpdater(mainThreadNamePrefix.await())
            )

            Component(env)('a'..'d').take('d' - 'a').collect()
        }

}

@Suppress("UNUSED_PARAMETER")
private fun <T> noOpSink(t: T) = Unit

private fun ThrowingInitializer(
    th: Throwable,
): Initializer<Nothing, Nothing> = { throw th }

private fun <M, S> CheckingUpdater(
    mainThreadName: String,
): Updater<M, S, Nothing> = { _, s ->

    val actualThreadNamePrefix = currentThreadName().replaceAfterLast('@', "")
    val mainThreadNamePrefix = mainThreadName.replaceAfterLast('@', "")

    assertNotEquals(mainThreadNamePrefix, actualThreadNamePrefix)

    s.noCommand()
}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
private suspend fun <C> NoOpResolver(): Resolver<C, Char> = object : Resolver<C, Char> {
    override fun invoke(command: C, context: ResolveCtx<Char>) = Unit
}

private val CharRange.size: Int
    get() = 1 + abs(last - first)

private suspend fun Component<Char, String, Char>.collectRanged(
    messages: CharRange,
) = this(messages).take(messages.size + 1/*plus initial snapshot*/).collect()

private fun <T> ThrowingResolver() = object : Resolver<Any?, T> {
    override fun invoke(command: Any?, context: ResolveCtx<T>) {
        throw ComponentException("Unexpected command $command")
    }
}
