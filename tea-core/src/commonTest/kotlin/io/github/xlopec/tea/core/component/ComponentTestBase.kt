package io.github.xlopec.tea.core.component

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Env
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.misc.CheckingUpdater
import io.github.xlopec.tea.core.misc.ComponentException
import io.github.xlopec.tea.core.misc.SnapshotsCollector
import io.github.xlopec.tea.core.misc.ThrowingInitializer
import io.github.xlopec.tea.core.misc.collectRanged
import io.github.xlopec.tea.core.misc.currentThreadName
import io.github.xlopec.tea.core.misc.expectCompletionAndCancel
import io.github.xlopec.tea.core.misc.runTestCancellingChildren
import io.github.xlopec.tea.core.misc.size
import io.github.xlopec.tea.core.misc.testEnv
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.core.with
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UnnecessaryAbstractClass")
abstract class ComponentTestBase(
    val factory: (env: Env<Char, String, Char>) -> Component<Char, String, Char>,
) {

    @Test
    fun `when subscriber disconnects then component initializer is re-invoked`() = runTestCancellingChildren {
        var counter = 0
        val initial = Initial<String, Char>(currentState = "", commands = setOf())
        val env = testEnv(
            initializer = { counter++; initial },
            resolver = { _ -> },
            updater = { m: Char, str -> (str + m).command(m) },
            scope = this
        )

        val component = factory(env)

        component.collectRanged('a'..'f')
        component.collectRanged('g'..'k')
        // each time new subscriber attaches to a component
        // with no subscribers initializer should be invoked
        assertEquals(2, counter, "Counter should be equal 2")
    }

    @Test
    fun `when component receives input then it emits correct sequence of snapshots`() = runTestCancellingChildren {
        val env = testEnv<Char, String, Char>(
            initializer = Initializer(""),
            resolver = { snapshot -> contextOf<CoroutineScope>().launch { snapshot.collect { check(it.commands.isEmpty()) { "Non empty snapshot $snapshot" } } } },
            updater = { m, _ -> m.toString().noCommand() },
            scope = this
        )

        val messages = arrayOf('a', 'b', 'c')
        val actualSnapshots = factory(env)(*messages).take(messages.size + 1).toList()
        val expectedSnapshots = listOf<Snapshot<Char, String, Char>>(
            Initial(currentState = "", commands = setOf()),
            Regular(currentState = "a", commands = setOf(), previousState = "", message = 'a'),
            Regular(currentState = "b", commands = setOf(), previousState = "a", message = 'b'),
            Regular(currentState = "c", commands = setOf(), previousState = "b", message = 'c')
        )

        assertContentEquals(expectedSnapshots, actualSnapshots)
    }

    @Test
    fun `when component receives input given recursive calculations then it emits correct sequence of snapshots`() =
        runTestCancellingChildren {

            val env = testEnv<Char, String, Char>(
                initializer = Initializer(""),
                resolver = { snapshot ->
                    contextOf<CoroutineScope>().launch {
                        snapshot.collect {
                            it.commands.forEach { ch ->
                                // only message 'b' should be consumed
                                effects { if (ch == 'a') ('b'..'d').toSet() else setOf() }
                            }
                        }
                    }
                },
                updater = { m, str -> (str + m).command(m) },
                scope = this
            )

            val actualSnapshots = factory(env)('a').take(3).toList()
            val expectedSnapshots = listOf(
                Initial(currentState = "", commands = setOf()),
                Regular(currentState = "a", commands = setOf('a'), previousState = "", message = 'a'),
                Regular(currentState = "ab", commands = setOf('b'), previousState = "a", message = 'b')
            )

            assertContentEquals(expectedSnapshots, actualSnapshots)
        }

    @Test
    fun `when attaching interceptor to component then original sequence of snapshots pipes through it`() = runTestCancellingChildren {
        val env = testEnv<Char, String, Char>(
            initializer = Initializer(""),
            resolver = { snapshot ->
                contextOf<CoroutineScope>().launch {
                    snapshot.collect {
                        it.commands.forEach { c ->
                            effects { setOf(c) }
                        }
                    }
                }
            },
            updater = { m, _ -> m.toString().noCommand() },
            scope = this
        )

        val sink = mutableListOf<Snapshot<Char, String, Char>>()
        val component = factory(env) with sink::add
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList()

        assertContentEquals(snapshots, sink)
    }

    @Test
    fun `when component has multiple consumers then snapshots are shared among them`() = runTestCancellingChildren {
        val env = testEnv<Char, String, Char>(
            initializer = Initializer(""),
            resolver = { snapshot ->
                contextOf<CoroutineScope>().launch {
                    snapshot.collect {
                        it.commands.forEach { ch ->
                            effects {
                                if (ch == 'a') {
                                    setOf(
                                        ch + 1, // only this message should be consumed
                                        ch + 2,
                                        ch + 3
                                    )
                                } else {
                                    setOf()
                                }
                            }
                        }
                    }
                }
            },
            updater = { m, str -> (str + m).command(m) },
            scope = this
        )

        val take = 3
        val component = factory(env)

        turbineScope {
            val consumer1 = component(emptyFlow()).take(take).testIn(this)
            val consumer2 = component('a').take(take).testIn(this)

            val expectedSnapshots = listOf(
                Initial(currentState = "", commands = setOf()),
                Regular(currentState = "a", commands = setOf('a'), previousState = "", message = 'a'),
                Regular(currentState = "ab", commands = setOf('b'), previousState = "a", message = 'b')
            )

            expectedSnapshots.forEach { expectedSnapshot ->
                assertEquals(expectedSnapshot, consumer2.awaitItem())
            }
            expectedSnapshots.forEach { expectedSnapshot ->
                assertEquals(expectedSnapshot, consumer1.awaitItem())
            }

            consumer2.expectCompletionAndCancel()
            consumer1.expectCompletionAndCancel()
        }
    }

    @Test
    fun `when component has multiple consumers then component is initialized only once`() = runTestCancellingChildren {
        var invocations = 0
        val env = testEnv<Char, String, Char>(
            initializer = { invocations++; yield(); Initial("bar", setOf()) },
            resolver = { snapshot -> contextOf<CoroutineScope>().launch { snapshot.collect { check(it.commands.isEmpty()) { "Non empty snapshot $it" } } } },
            updater = { _, s -> s.noCommand() },
            scope = this,
            // SharingStarted.Lazily since in case of default option replay
            // cache will be disposed immediately causing test to fail
            shareOptions = { scope, upstream ->
                upstream.shareIn(scope, SharingStarted.Lazily, 1)
            }
        )

        val component = factory(env)
        // Ensure component builder won't invoke initializer before first consumer arrives
        assertEquals(0, invocations)

        repeat(1_000) { launch { component('a').first() } }

        advanceUntilIdle()
        assertEquals(1, invocations)
    }

    @Test
    fun `test component's job gets canceled properly`() = runTestCancellingChildren {
        val resolver = SnapshotsCollector<Char, String, Char>()
        val env = testEnv(
            initializer = Initializer(""),
            resolver = { snapshots -> resolver.collect(snapshots) },
            updater = { message, state -> state command message },
            scope = this
        )

        val messages = 'a'..'z'
        // plus initial snapshot
        factory(env)(messages).take(messages.size + 1).collect()

        resolver.messages.consumeAsFlow().test {
            assertEquals(Initial(""), awaitItem())
            messages.forEach {
                assertEquals(Regular("", setOf(it), "", it), awaitItem())
            }
            expectNoEvents()
        }
    }

    @Test
    fun `when component has multiple consumers then it can serve multiple message sources`() = runTestCancellingChildren {
        val env = testEnv<Char, String, Char>(
            initializer = Initializer(""),
            resolver = { snapshot -> contextOf<CoroutineScope>().launch { snapshot.collect { check(it.commands.isEmpty()) { "Non empty snapshot $it" } } } },
            updater = { m, _ -> m.toString().noCommand() },
            scope = this
        )

        val range = 'a'..'h'
        val component = factory(env)

        val chan1 = Channel<Char>()
        val chan2 = Channel<Char>()

        turbineScope {
            val consumer2 = component(chan2.consumeAsFlow()).take(1 + range.count()).testIn(this)
            val consumer1 = component(chan1.consumeAsFlow()).take(1 + range.count()).testIn(this)

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
                        currentState = "",
                        commands = setOf<Char>()
                    )
                ) + range.mapIndexed { index, ch ->
                    Regular(
                        currentState = ch.toString(),
                        commands = setOf(),
                        previousState = if (index == 0) "" else ch.dec().toString(),
                        message = ch
                    )
                }

            expectedSnapshots.forEach { expectedSnapshot ->
                assertEquals(expectedSnapshot, consumer1.awaitItem())
            }

            expectedSnapshots.forEach { expectedSnapshot ->
                assertEquals(expectedSnapshot, consumer2.awaitItem())
            }

            consumer1.expectCompletionAndCancel()
            consumer2.expectCompletionAndCancel()
        }
    }

    @Test
    fun `when collecting component given updater throws exception then it is handled by coroutine scope`() {
        val scope = TestScope(UnconfinedTestDispatcher(name = "Failing host scope"))

        val component = Component(
            initializer = Initializer("", "a"),
            resolver = { snapshot -> scope.launch { snapshot.collect { /* no-op */ } } },
            updater = { m: String, s -> throw ComponentException("message=$m, state=$s") },
            scope = scope
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
            Env<String, Nothing, Nothing>(
                initializer = ThrowingInitializer(expectedException),
                resolver = { snapshot -> scope.launch { snapshot.collect { /* no-op */ } } },
                updater = { _, s -> s },
                scope = scope
            )
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
    fun `when collecting component with specific dispatcher then updater runs on this dispatcher`() = runTestCancellingChildren {
        // All test schedulers use 'Test worker' as prefix, so to work around this issue we use
        // custom dispatcher with different thread naming strategy
        val mainThreadNamePrefix = async { currentThreadName() }
        val scope = CoroutineScope(coroutineContext + Job(coroutineContext[Job.Key]) + Dispatchers.Default)
        val env = testEnv<Char, String, Char>(
            initializer = Initializer(""),
            resolver = { snapshot -> contextOf<CoroutineScope>().launch { snapshot.collect { check(it.commands.isEmpty()) { "Non empty snapshot $it" } } } },
            updater = CheckingUpdater(mainThreadNamePrefix.await()),
            scope = scope,
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
        scope.cancel()
    }
}
