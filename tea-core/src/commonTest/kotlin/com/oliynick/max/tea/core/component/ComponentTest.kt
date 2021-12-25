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

@file:Suppress("FunctionName", "TestFunctionName")
@file:OptIn(
    ExperimentalCoroutinesApi::class,
    InternalCoroutinesApi::class,
    UnstableApi::class,
    ExperimentalStdlibApi::class
)

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.misc.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.test.*
import kotlin.Long.Companion.MAX_VALUE
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.test.*

class ComponentTest {

    @Deprecated("replace", ReplaceWith("Component"))
    val factory: CoroutineScope.(Env<Char, String, Char>) -> Component<Char, String, Char> =
        { Component(it) }

    companion object {

        //val TestTimeout: Timeout = Timeout.seconds(10L)
        const val ThreadName = "test thread"

        val SingleThreadDispatcher = Dispatchers.Default.limitedParallelism(1)
        //    Executors.newSingleThreadExecutor { r -> Thread(r, ThreadName) }
        //       .asCoroutineDispatcher()
    }

    //@get:Rule
    //var globalTimeout: Timeout = TestTimeout

    @Test
    fun `when subscriber disconnects, then component initializer is re-invoked`() =
        runTestCancellingChildren {

            var counter = 0
            val initial = Initial<String, Char>("", setOf())
            val env = TestEnv<Char, String, Char>(
                { counter++; initial },
                ::noOpResolver,
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
    fun `when upstream receives new input, then previous downstream is canceled`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ::noOpResolver,
                ::messageAsCommand
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

            val actualStates = env.upstream(initialStates.asFlow(), ::noOpSink, ::testInput)
                .toList()

            val (state, commands) = lastInitial
            val expectedStates = initialStates + Regular(state, commands, state, commands.first())

            assertContentEquals(expectedStates, actualStates)
        }

    @Test
    fun `when component receives input, then it emits correct sequence of snapshots`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ::throwingResolver,
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
    fun `when component receives input, given recursive calculations, then it emits correct sequence of snapshots`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { ch ->
                    // only message 'b' should be consumed
                    if (ch == 'a') ('b'..'d').toSet() else setOf()
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
    fun `when attaching interceptor to component, then original sequence of snapshots pipes through it`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { c -> setOf(c) },
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
    fun `when component has multiple consumers, then snapshots are shared among them`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { ch ->
                    if (ch == 'a') setOf(
                        ch + 1,// only this message should be consumed
                        ch + 2,
                        ch + 3
                    ) else setOf()
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
    fun `when component has multiple consumers, then component is initialized only once`() =
        runTestCancellingChildren {

            var invocations = 0
            val env = TestEnv<Char, String, Char>(
                { invocations++; yield(); Initial("bar", setOf()) },
                ::throwingResolver,
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

            val resolver = ForeverWaitingResolver<Char>()
            val env = TestEnv(
                Initializer(""),
                resolver::resolveForever,
                ::messageAsCommand
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
    fun `when component has multiple consumers, then it can serve multiple message sources`() =
        runTestCancellingChildren {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ::throwingResolver,
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
    fun `test resolver runs on a given dispatcher`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                CheckingResolver(coroutineDispatcher),
                ::messageAsCommand
            )

            factory(env)('a'..'d')
                .take('d' - 'a').collect()
        }

    @Test
    fun `when collecting component, given updater throws exception, then it's handled by coroutine scope`() {
        val scope = TestScope(UnconfinedTestDispatcher(name = "Failing host scope"))

        val component = Component<String, String, String>(
            Initializer("", "a"),
            ::throwingResolver,
            ::throwingUpdater,
            scope,
            scope.coroutineDispatcher,
            scope.coroutineDispatcher
        )

        val job = scope.launch { component("").collect() }

        assertTrue(job.isCancelled)

        val th = job.getCancellationException().cause

        assertTrue("Cancellation cause $th") { th is ComponentException }
        assertTrue(!scope.isActive)
    }

    @Test
    fun `test if initializer fails with exception it gets handled by coroutine scope`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {
            val expectedException = RuntimeException("hello")
            val component = Component<String, String, String>(
                ThrowingInitializer(expectedException),
                ::throwingResolver,
                ::throwingUpdater,
                this@runTest
            )

            val job = launch { component("").collect() }

            job.join()
            assertTrue(job.isCancelled)
            //job.isCancelled.shouldBeTrue()

            val th = job.getCancellationException().cause

            assertTrue("Cancellation cause $th") {
                th is RuntimeException && th.message == expectedException.message
            }

/*withClue("Cancellation cause $th") {
                th.shouldNotBeNull()
                th.shouldBeTypeOf<RuntimeException>()
                th.shouldHaveMessage(expectedException.message!!)
            }*/


            //isActive.shouldBeFalse()
            assertTrue(!isActive)
        }

    @Test
    fun `test initializer runs on a given dispatcher`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                CheckingInitializer(SingleThreadDispatcher),
                ::throwingResolver,
                { m, _ -> m.toString().noCommand() },
                io = SingleThreadDispatcher
            )

            factory(env)('a'..'d').take('d' - 'a').collect()
        }

    @Test
    fun `test updater runs on a given dispatcher`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ::throwingResolver,
                CheckingUpdater(Regex("$ThreadName @coroutine#\\d+")),
                computation = SingleThreadDispatcher
            )

            factory(env)('a'..'d').take('d' - 'a').collect()
        }

}

@Suppress("UNUSED_PARAMETER")
private fun <T> noOpSink(t: T) = Unit

private fun CheckingInitializer(
    expectedDispatcher: CoroutineDispatcher,
): Initializer<String, Nothing> = {
    assertTrue { coroutineContext[ContinuationInterceptor] === expectedDispatcher }
    Initial("", setOf())
}

private fun CheckingResolver(
    expectedDispatcher: CoroutineDispatcher,
): Resolver<Any?, Nothing> = {
    assertTrue { coroutineContext[CoroutineDispatcher.Key] === expectedDispatcher }
    setOf()
}

fun ThrowingInitializer(
    th: Throwable,
): Initializer<Nothing, Nothing> = { throw th }

private fun currentThreadName(): String =
    TODO()//Thread.currentThread().name

private fun <M, S> CheckingUpdater(
    expectedThreadGroup: Regex,
): Updater<M, S, Nothing> = { _, s ->

    val threadName = currentThreadName()

    assertTrue("Thread name should match '${expectedThreadGroup.pattern}' but was '$threadName'") {
        threadName.matches(expectedThreadGroup)
    }

/*withClue("Thread name should match '${expectedThreadGroup.pattern}' but was '$threadName'") {
        threadName.matches(expectedThreadGroup).shouldBeTrue()
    }*/

    s.noCommand()
}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
private suspend fun <T> noOpResolver(
    m: T,
): Set<Nothing> = setOf()

class ForeverWaitingResolver<T> {

    private val _messages = Channel<T>()

    val messages: ReceiveChannel<T> = _messages

    suspend fun resolveForever(
        t: T,
    ): Nothing {

        try {
            delay(MAX_VALUE)
        } finally {
            withContext(NonCancellable) {
                _messages.send(t)
            }
        }

        error("Improper cancellation, message=$t")
    }
}

private val CharRange.size: Int
    get() = 1 + abs(last - first)

const val TestTimeoutMillis = 10 * 1000L

/**
 * Same as [runTest] but cancels child jobs before leaving test.
 * This is useful when testing running [Component] since component's upstream doesn't get
 * destroyed until host scope is canceled
 */
fun runTestCancellingChildren(
    context: CoroutineContext = EmptyCoroutineContext,
    dispatchTimeoutMs: Long = TestTimeoutMillis,
    testBody: suspend TestScope.() -> Unit
): TestResult = runTest(context, dispatchTimeoutMs) {
    testBody()
    job.cancelChildren()
}

inline val CoroutineScope.job: Job
    get() = coroutineContext[Job.Key] ?: error("scope doesn't have job $this")

private suspend fun Component<Char, String, Char>.collectRanged(
    messages: CharRange,
) = this(messages).take(messages.size + 1/*plus initial snapshot*/).collect()