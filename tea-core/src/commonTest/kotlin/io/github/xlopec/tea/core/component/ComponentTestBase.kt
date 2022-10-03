package io.github.xlopec.tea.core.component

import app.cash.turbine.test
import app.cash.turbine.testIn
import io.github.xlopec.tea.core.*
import io.github.xlopec.tea.core.misc.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

abstract class ComponentTestBase(
    val factory: (env: Env<Char, String, Char>) -> Component<Char, String, Char>,
) {

    @Test
    fun `when subscriber disconnects then component initializer is re-invoked`() = runTestCancellingChildren {
        var counter = 0
        val initial = Initial<String, Char>("", setOf())
        val env = testEnv(
            { counter++; initial },
            { _, _ -> },
            { m: Char, str -> (str + m).command(m) },
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
            Initializer(""),
            { snapshot, _ -> check(snapshot.commands.isEmpty()) { "Non empty snapshot $snapshot" } },
            { m, _ -> m.toString().noCommand() }
        )

        val messages = arrayOf('a', 'b', 'c')
        val actualSnapshots = factory(env)(*messages).take(messages.size + 1).toList()
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

            val env = testEnv<Char, String, Char>(
                Initializer(""),
                { snapshot, ctx ->
                    snapshot.commands.forEach { ch ->
                        // only message 'b' should be consumed
                        ctx effects { if (ch == 'a') ('b'..'d').toSet() else setOf() }
                    }
                },
                { m, str -> (str + m).command(m) }
            )

            val actualSnapshots = factory(env)('a').take(3).toList()
            val expectedSnapshots = listOf(
                Initial("", setOf()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )

            assertContentEquals(expectedSnapshots, actualSnapshots)
        }

    @Test
    fun `when attaching interceptor to component then original sequence of snapshots pipes through it`() = runTestCancellingChildren {
        val env = testEnv<Char, String, Char>(
            Initializer(""),
            { snapshot, ctx ->
                snapshot.commands.forEach { c ->
                    ctx effects { setOf(c) }
                }
            },
            { m, _ -> m.toString().noCommand() },
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
            Initializer(""),
            { snapshot, ctx ->
                snapshot.commands.forEach { ch ->
                    ctx effects {
                        if (ch == 'a') setOf(
                            ch + 1, // only this message should be consumed
                            ch + 2,
                            ch + 3
                        ) else setOf()
                    }
                }
            },
            { m, str -> (str + m).command(m) }
        )

        val take = 3
        val component = factory(env)

        val consumer1 = component(emptyFlow()).take(take).testIn(this)
        val consumer2 = component('a').take(take).testIn(this)

        val expectedSnapshots = listOf(
            Initial("", setOf()),
            Regular("a", setOf('a'), "", 'a'),
            Regular("ab", setOf('b'), "a", 'b')
        )

        expectedSnapshots.forEach { expectedSnapshot ->
            assertEquals(expectedSnapshot, consumer2.awaitItem())
        }
        expectedSnapshots.forEach { expectedSnapshot ->
            assertEquals(expectedSnapshot, consumer1.awaitItem())
        }

        consumer2.expectNoEventsAndCancel()
        consumer1.expectNoEventsAndCancel()
    }

    @Test
    fun `when component has multiple consumers then component is initialized only once`() = runTestCancellingChildren {
        var invocations = 0
        val env = testEnv<Char, String, Char>(
            { invocations++; yield(); Initial("bar", setOf()) },
            { snapshot, _ -> check(snapshot.commands.isEmpty()) { "Non empty snapshot $snapshot" } },
            { _, s -> s.noCommand() },
            // SharingStarted.Lazily since in case of default option replay
            // cache will be disposed immediately causing test to fail
            shareOptions = ShareOptions(SharingStarted.Lazily, 1U)
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
        val resolver = ForeverWaitingResolver<Char, String, Char>()
        val env = testEnv(
            Initializer(""),
            resolver,
            { message, state -> state command message }
        )

        val messages = 'a'..'z'

        factory(env)(messages).take(messages.size + 1 /*plus initial snapshot*/).collect()

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
            Initializer(""),
            { snapshot, _ -> check(snapshot.commands.isEmpty()) { "Non empty snapshot $snapshot" } },
            { m, _ -> m.toString().noCommand() }
        )

        val range = 'a'..'h'
        val component = factory(env)

        val chan1 = Channel<Char>()
        val chan2 = Channel<Char>()

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

        expectedSnapshots.forEach { expectedSnapshot ->
            assertEquals(expectedSnapshot, consumer1.awaitItem())
        }

        expectedSnapshots.forEach { expectedSnapshot ->
            assertEquals(expectedSnapshot, consumer2.awaitItem())
        }

        consumer1.expectNoEventsAndCancel()
        consumer2.expectNoEventsAndCancel()
    }

    @Test
    fun `when collecting component given updater throws exception then it is handled by coroutine scope`() {
        val scope = TestScope(UnconfinedTestDispatcher(name = "Failing host scope"))

        val component = Component(
            Initializer("", "a"),
            { snapshot, _ -> throw ComponentException("Unexpected snapshot $snapshot") },
            { m: String, s -> throw ComponentException("message=$m, state=$s") },
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
            Env<String, Nothing, Nothing>(
                initializer = ThrowingInitializer(expectedException),
                resolver = { snapshot, _ -> throw ComponentException("Unexpected snapshot $snapshot") },
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
        val env = CoroutineScope(Dispatchers.Default).testEnv<Char, String, Char>(
            Initializer(""),
            { snapshot, _ -> check(snapshot.commands.isEmpty()) { "Non empty snapshot $snapshot" } },
            CheckingUpdater(mainThreadNamePrefix.await())
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
    }
}
