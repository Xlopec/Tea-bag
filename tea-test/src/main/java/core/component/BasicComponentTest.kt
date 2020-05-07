@file:Suppress("FunctionName")

package core.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import core.misc.messageAsCommand
import core.misc.throwingResolver
import core.scope.runBlockingInTestScope
import io.kotlintest.matchers.asClue
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext

abstract class BasicComponentTest(
    protected val factory: CoroutineScope.(Env<Char, String, Char>) -> Component<Char, String, Char>
) {

    @Test
    fun `test component emits a correct sequence of snapshots`() = runBlocking {

        val env = Env<Char, String, Char>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.toString().noCommand() }
        )

        val component = factory(env)
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList(ArrayList(messages.size + 1))

        snapshots shouldContainExactly listOf(
            Initial("", emptySet()),
            Regular("a", emptySet(), "", 'a'),
            Regular("b", emptySet(), "a", 'b'),
            Regular("c", emptySet(), "b", 'c')
        )
    }

    @Test
    fun `test component emits a correct sequence of snapshots if initial commands were present`() =
        runBlocking {

            val env = Env<Char, String, Char>(
                Initializer("", 'a', 'b', 'c'),
                { c -> setOf(c) },
                { m, _ -> m.toString().noCommand() }
            )

            val component = factory(env)
            val messages = arrayOf('d', 'e', 'f')
            val snapshots =
                component(*messages).take(3 + messages.size + 1)
                    .toList(ArrayList(3 + messages.size + 1))

            snapshots shouldContainExactly listOf(
                Initial("", setOf('a', 'b', 'c')),
                Regular("a", emptySet(), "", 'a'),
                Regular("b", emptySet(), "a", 'b'),
                Regular("c", emptySet(), "b", 'c'),
                Regular("d", emptySet(), "c", 'd'),
                Regular("e", emptySet(), "d", 'e'),
                Regular("f", emptySet(), "e", 'f')
            )
        }

    @Test
    fun `test component emits a correct sequence of snapshots if we have recursive calculations`() =
        runBlocking {

            val env = Env<Char, String, Char>(
                "",
                { ch ->
                    if (ch == 'a') setOf(
                        ch + 1,// only this message should be consumed
                        ch + 2,
                        ch + 3
                    ) else emptySet()
                },
                { m, str -> (str + m).command(m) }
            )

            val component = factory(env)
            val snapshots = component('a').take(3).toCollection(ArrayList())

            @Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
            snapshots shouldBe listOf<Snapshot<Char, String, Char>>(
                Initial("", emptySet()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )
        }

    @Test
    fun `test component emits a correct sequence of snapshots if update returns set of messages`() =
        runBlocking {

            val env = Env<Char, String, Char>(
                "",
                { ch -> setOf(ch) },
                { m, str ->
                    (str + m).command(
                        if (str.isEmpty()) setOf(
                            'b',
                            'c'
                        ) else emptySet()
                    )
                }
            )

            val component = factory(env)
            val snapshots = component('a').take(3).toCollection(ArrayList())

            snapshots shouldContainExactly listOf(
                Initial("", emptySet()),
                Regular("a", setOf('b', 'c'), "", 'a'),
                Regular("ab", emptySet(), "a", 'b')
            )
        }

    @Test
    fun `test interceptor sees an original sequence of snapshots`() = runBlocking {

        val env = Env<Char, String, Char>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.toString().noCommand() }
        )

        val sink = mutableListOf<Snapshot<Char, String, Char>>()
        val component = factory(env) with { sink.add(it) }
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList(ArrayList(messages.size + 1))

        sink shouldContainExactly snapshots
    }

    @Test
    fun `test component's snapshots shared among consumers`() = runBlocking {

        val env = Env<Char, String, Char>(
            "",
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
            async {
                component(emptyFlow()).take(take)
                    .toCollection(ArrayList())
            }
        val snapshots1Deferred = async {
            component('a').take(take).toCollection(ArrayList())
        }

        @Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
        val expected = listOf<Snapshot<Char, String, Char>>(
            Initial("", emptySet()),
            Regular("a", setOf('a'), "", 'a'),
            Regular("ab", setOf('b'), "a", 'b')
        )

        snapshots1Deferred.await().asClue { it shouldContainExactly expected }
        snapshots2Deferred.await().asClue { it shouldContainExactly expected }
    }

    @Test
    fun `test component gets initialized only once if we have multiple consumers`() =
        runBlocking {

            val countingInitializer = object {

                val invocations = atomic(0)

                fun initializer(): Initializer<String, Nothing> = {
                    invocations.incrementAndGet()
                    yield()
                    Initial("bar", emptySet())
                }
            }

            val env = Env<Char, String, Char>(
                countingInitializer.initializer(),
                ::throwingResolver,
                { _, s -> s.noCommand() }
            )

            val component = factory(env)

            countingInitializer.invocations.value shouldBe 0

            val coroutines = 100
            val jobs = (0 until coroutines).map { launch { component('a').first() } }
                .toCollection(ArrayList(coroutines))

            jobs.joinAll()

            countingInitializer.invocations.value shouldBe 1
        }

    @Test
    fun `test component's job gets canceled properly`() = runBlocking {

        val env = Env<Char, String, Char>(
            "",
            ::foreverWaitingResolver,
            { m, _ -> m.toString().command(m) }
        )

        val component = factory(env)
        val job = launch { component('a', 'b', 'c').toList(ArrayList()) }

        yield()
        job.cancel()

        job.isActive.shouldBeFalse()
        isActive.shouldBeTrue()
    }

    @Test
    fun `test component doesn't block if serves multiple message sources`() =
        runBlockingInTestScope {

            val env = Env<Char, String, Char>(
                "",
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
                    .toCollection(ArrayList())
            }

            val snapshots1Deferred = async {
                component(chan1.consumeAsFlow())
                    .take(1 + range.count())
                    .toCollection(ArrayList())
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

            snapshots1Deferred.await().asClue { it shouldContainExactly expected }
            snapshots2Deferred.await().asClue { it shouldContainExactly expected }
        }

    @Test
    fun `test resolver runs on a given dispatcher`() = runBlockingInTestScope {

        val testDispatcher = Dispatchers.Unconfined

        val env = Env<Char, String, Char>(
            Initializer(""),
            CheckingResolver(testDispatcher),
            ::messageAsCommand,
            testDispatcher
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
    }

    @Test
    fun `test initializer runs on a given dispatcher`() = runBlockingInTestScope {

        val testDispatcher = Dispatchers.Unconfined

        val env = Env<Char, String, Char>(
            CheckingInitializer(testDispatcher),
            ::throwingResolver,
            { m, _ -> m.toString().noCommand() },
            testDispatcher
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
    }

    @Test
    fun `test updater runs on a given dispatcher`() = runBlockingInTestScope {

        val testDispatcher = Dispatchers.Unconfined
        val expectedDispatcherName = withContext(testDispatcher) { currentThreadGroupName() }

        val env = Env<Char, String, Char>(
            Initializer(""),
            ::throwingResolver,
            CheckingUpdater(expectedDispatcherName),
            computation = testDispatcher
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
    }

}

private fun CheckingInitializer(
    expectedDispatcher: CoroutineDispatcher
): Initializer<String, Nothing> = {
    coroutineContext[ContinuationInterceptor] shouldBeSameInstanceAs expectedDispatcher
    Initial("", emptySet())
}

private fun CheckingResolver(
    expectedDispatcher: CoroutineDispatcher
): Resolver<Any?, Nothing> = {
    coroutineContext[ContinuationInterceptor] shouldBeSameInstanceAs expectedDispatcher
    emptySet()
}

private fun currentThreadGroupName(): String =
    Thread.currentThread().threadGroup.name

private fun <M, S> CheckingUpdater(
    expectedThreadGroup: String
): Updater<M, S, Nothing> = { _, s ->
    currentThreadGroupName() shouldBe expectedThreadGroup
    s.noCommand()
}

private suspend fun <T> foreverWaitingResolver(
    m: T
): Nothing {

    delay(Long.MAX_VALUE)

    error("Improper cancellation, message=$m")
}

