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
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
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

            suspend fun Component<Char, String, Char>.collectRange(
                messages: CharRange,
            ) = this(messages).take(messages.size + 1/*plus initial snapshot*/).collect()

            component.collectRange('a'..'f')
            component.collectRange('g'..'k')
            // each time new subscriber attaches to a component
            // with no subscribers initializer should be invoked
            assertEquals(2, counter, "Counter should be equal 2")
        }

    @Test
    fun `test when receiving new input previous downstream gets canceled`() =
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
                        delay(Long.MAX_VALUE)
                    }
                }

            val resultingStates = env.upstream(initialStates.asFlow(), ::noOpSink, ::testInput)
                .toList()

            val (state, commands) = lastInitial

            assertContentEquals(
                (initialStates + Regular(
                    state,
                    commands,
                    state,
                    commands.first()
                )),
                resultingStates
            )

            assertContentEquals(
                (initialStates + Regular(
                    state,
                    commands,
                    state,
                    commands.first()
                )),

                resultingStates
            )

/*resultingStates shouldContainExactly (initialStates + Regular(state,
                commands,
                state,
                commands.first()))*/

        }

    @Test
    fun `test component emits a correct sequence of snapshots`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ::throwingResolver,
                { m, _ -> m.toString().noCommand() }
            )

            val messages = arrayOf('a', 'b', 'c')
            val snapshots = factory(env)(*messages).take(messages.size + 1).toList()

            assertContentEquals(
                listOf(
                    Initial("", emptySet()),
                    Regular("a", emptySet(), "", 'a'),
                    Regular("b", emptySet(), "a", 'b'),
                    Regular("c", emptySet(), "b", 'c')
                ),

                snapshots
            )
/*snapshots.shouldContainExactly(
            Initial("", emptySet()),
            Regular("a", emptySet(), "", 'a'),
            Regular("b", emptySet(), "a", 'b'),
            Regular("c", emptySet(), "b", 'c')
        )*/
        }

    @Test
    fun `test component emits a correct sequence of snapshots if we have recursive calculations`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { ch ->
                    // only message 'b' should be consumed
                    if (ch == 'a') ('b'..'d').toSet() else emptySet()
                },
                { m, str -> (str + m).command(m) }
            )

            val snapshots = factory(env)('a').take(3).toList()

            assertContentEquals(
                listOf<Snapshot<Char, String, Char>>(
                    Initial("", emptySet()),
                    Regular("a", setOf('a'), "", 'a'),
                    Regular("ab", setOf('b'), "a", 'b')
                ),

                snapshots
            )

/*@Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
            snapshots shouldBe listOf<Snapshot<Char, String, Char>>(
                Initial("", emptySet()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )*/

        }

    @Test
    fun `test interceptor sees an original sequence of snapshots`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { c -> setOf(c) },
                { m, _ -> m.toString().noCommand() },
            )

            val sink = mutableListOf<Snapshot<Char, String, Char>>()
            val component = factory(env) with sink::add
            val messages = arrayOf('a', 'b', 'c')
            val snapshots =
                component(*messages).take(messages.size + 1).toList()

            assertContentEquals(snapshots, sink)
            //sink shouldContainExactly snapshots
        }

    @Test
    fun `test component's snapshots shared among consumers`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                { ch ->
                    if (ch == 'a') setOf(
                        ch + 1,// only this message should be consumed
                        ch + 2,
                        ch + 3
                    ) else emptySet()
                },
                { m, str -> (str + m).command(m) }
            )

            val take = 3
            val component = factory(env)
            val snapshots2Deferred =
                GlobalScope.async {
                    component(emptyFlow()).take(take)
                        .toList()
                }
            val snapshots1Deferred = GlobalScope.async {
                component('a').take(take).toList()
            }

            @Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
            val expected = listOf<Snapshot<Char, String, Char>>(
                Initial("", emptySet()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )

            val snapshots1 = snapshots1Deferred.await()
            val snapshots2 = snapshots2Deferred.await()

            assertContentEquals(
                expected, snapshots1, """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
            )
            assertContentEquals(
                expected, snapshots2, """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
            )

/*withClue(
            """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
        ) {
            snapshots1 shouldContainExactly expected
            snapshots2 shouldContainExactly expected
        }*/

        }

    @Test
    fun `test component gets initialized only once if we have multiple consumers`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val countingInitializer = object {

                val invocations = atomic(0)

                fun initializer(): Initializer<String, Nothing> = {
                    invocations.incrementAndGet()
                    yield()
                    Initial("bar", emptySet())
                }
            }

            val env = TestEnv<Char, String, Char>(
                countingInitializer.initializer(),
                ::throwingResolver,
                { _, s -> s.noCommand() },
                io = Dispatchers.Default,//IO,
                // SharingStarted.Lazily since in case of default option the replay
                // cache will be disposed immediately causing test to fail
                shareOptions = ShareOptions(SharingStarted.Lazily, 1U)
            )

            val component = factory(env)

            assertEquals(0, countingInitializer.invocations.value)
            //countingInitializer.invocations.value shouldBe 0

            val coroutines = 1_000
            val jobs = (0 until coroutines).map { launch { component('a').first() } }
                .toCollection(ArrayList(coroutines))

            jobs.joinAll()

            assertEquals(1, countingInitializer.invocations.value)

            //countingInitializer.invocations.value shouldBe 1
        }

    @Test
    //@Ignore("Ignored due to https://youtrack.jetbrains.com/issue/KT-47195")
    fun `test component's job gets canceled properly`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val resolver = ForeverWaitingResolver<Char>()
            val env = TestEnv(
                Initializer(""),
                resolver::resolveForever,
                ::messageAsCommand
            )

            val messages = 'a'..'z'

            factory(env)(messages).take(
                messages.size + 1
//plus initial snapshot
            ).collect()

            val canceled = resolver.messages
                .consumeAsFlow()
                .take(messages.size)
                .toList()

            assertEquals(messages.toList(), canceled)
            // canceled shouldContainExactlyInAnyOrder messages.toList()
        }

    @Test
    fun `test component doesn't block if serves multiple message sources`() =
        runTest(dispatchTimeoutMs = TestTimeoutMillis) {

            val env = TestEnv<Char, String, Char>(
                Initializer(""),
                ::throwingResolver,
                { m, _ -> m.toString().noCommand() }
            )

            val range = 'a'..'h'
            val component = factory(env)

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

            }

            val expected: List<Snapshot<Char, String, Char>> =
                listOf(
                    Initial(
                        "",
                        emptySet<Char>()
                    )
                ) + range.mapIndexed { index, ch ->
                    Regular(
                        ch.toString(),
                        emptySet(),
                        if (index == 0) "" else ch.dec().toString(),
                        ch
                    )
                }

            val snapshots1 = snapshots1Deferred.await()
            val snapshots2 = snapshots2Deferred.await()

            assertContentEquals(
                expected, snapshots1, """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
            )

            assertContentEquals(
                expected, snapshots2, """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
            )
/*withClue(
                """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
            ) {
                snapshots1 shouldContainExactly expected
                snapshots2 shouldContainExactly expected
            }*/

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
    Initial("", emptySet())
}

private fun CheckingResolver(
    expectedDispatcher: CoroutineDispatcher,
): Resolver<Any?, Nothing> = {
    assertTrue { coroutineContext[CoroutineDispatcher.Key] === expectedDispatcher }
    emptySet()
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
): Set<Nothing> = emptySet()

class ForeverWaitingResolver<T> {

    private val _messages = Channel<T>()

    val messages: ReceiveChannel<T> = _messages

    suspend fun resolveForever(
        t: T,
    ): Nothing {

        try {
            delay(Long.MAX_VALUE)
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
