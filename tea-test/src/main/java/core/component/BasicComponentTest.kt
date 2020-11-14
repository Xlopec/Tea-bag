@file:Suppress("FunctionName")

package core.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import core.misc.messageAsCommand
import core.misc.throwingResolver
import core.scope.coroutineDispatcher
import core.scope.runBlockingInTestScope
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

@OptIn(UnstableApi::class)
abstract class BasicComponentTest(
    protected val factory: CoroutineScope.(Env<Char, String, Char>) -> Component<Char, String, Char>,
) {

    private companion object {
        const val TestTimeoutMillis = 5_000L
        const val ThreadName = "test thread"

        val CoroutineDispatcher =
            Executors.newSingleThreadExecutor { r -> Thread(r, ThreadName) }
                .asCoroutineDispatcher()
    }

    @Test(timeout = TestTimeoutMillis)
    fun `test initializer is invoked each time for component with no active subscribers`() =
        runBlockingInTestScope {

            var counter = 0
            val initial = Initial<String, Char>("", emptySet())
            val env = Env<Char, String, Char>(
                { counter++; initial },
                ::noOpResolver,
                { m, str -> (str + m).command(m) },
                coroutineDispatcher,
                coroutineDispatcher,
            )

            val component = factory(env)

            suspend fun Component<Char, String, Char>.collect(
                messages: CharRange,
            ) = this(messages).take(messages.size + 1/*plus initial snapshot*/).collect()

            component.collect('a'..'f')
            component.collect('g'..'k')
            // each time new subscriber attaches to a component
            // with no subscribers initializer should be invoked
            counter shouldBe 2
        }

    @Test(timeout = TestTimeoutMillis)
    fun `test when receiving new input previous downstream gets canceled`() =
        runBlockingInTestScope {

            val env = Env<Char, String, Char>(
                Initializer(""),
                ::noOpResolver,
                ::messageAsCommand,
                coroutineDispatcher,
                coroutineDispatcher
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

            resultingStates shouldContainExactly(initialStates +
                    Regular(
                        state,
                        commands,
                        state,
                        commands.first()
                    )
            )
        }

    @Test(timeout = TestTimeoutMillis)
    fun `test component emits a correct sequence of snapshots`() = runBlocking {

        val env = Env<Char, String, Char>(
            "",
            ::throwingResolver,
            { m, _ -> m.toString().noCommand() }
        )

        val component = factory(env)
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList()

        snapshots.shouldContainExactly(
            Initial("", emptySet()),
            Regular("a", emptySet(), "", 'a'),
            Regular("b", emptySet(), "a", 'b'),
            Regular("c", emptySet(), "b", 'c')
        )
    }

    @Test(timeout = TestTimeoutMillis)
    fun `test component emits a correct sequence of snapshots if we have recursive calculations`() =
        runBlocking {

            val env = Env<Char, String, Char>(
                "",
                { ch ->
                    // only message 'b' should be consumed
                    if (ch == 'a') ('b'..'d').toSet() else emptySet()
                },
                { m, str -> (str + m).command(m) }
            )

            val snapshots = factory(env)('a').take(3).toList()

            @Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
            snapshots shouldBe listOf<Snapshot<Char, String, Char>>(
                Initial("", emptySet()),
                Regular("a", setOf('a'), "", 'a'),
                Regular("ab", setOf('b'), "a", 'b')
            )
        }

    @Test(timeout = TestTimeoutMillis)
    fun `test interceptor sees an original sequence of snapshots`() = runBlocking {

        val env = Env<Char, String, Char>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.toString().noCommand() }
        )

        val sink = mutableListOf<Snapshot<Char, String, Char>>()
        val component = factory(env) with sink::add
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList()

        sink shouldContainExactly snapshots
    }

    @Test(timeout = TestTimeoutMillis)
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
                    .toList()
            }
        val snapshots1Deferred = async {
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

        withClue(
            """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
        ) {
            snapshots1 shouldContainExactly expected
            snapshots2 shouldContainExactly expected
        }
    }

    @Test(timeout = TestTimeoutMillis)
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

    @Test(timeout = TestTimeoutMillis)
    fun `test component's job gets canceled properly`() = runBlockingInTestScope {

        val resolver = ForeverWaitingResolver<Char>()
        val env = Env(
            Initializer(""),
            resolver::resolveForever,
            ::messageAsCommand,
            coroutineDispatcher,
            coroutineDispatcher
        )

        val messages = 'a'..'z'

        factory(env)(messages).take(messages.size + 1/*plus initial snapshot*/).collect()

        val canceled = resolver.messages
            .consumeAsFlow()
            .take(messages.size)
            .toList()

        canceled shouldContainExactlyInAnyOrder messages.toList()
    }

    @Test(timeout = TestTimeoutMillis)
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

            withClue(
                """
            snapshots1: $snapshots1
            snapshots2: $snapshots2
            expected: $expected
            """.trimIndent()
            ) {
                snapshots1 shouldContainExactly expected
                snapshots2 shouldContainExactly expected
            }
        }

    @Test(timeout = TestTimeoutMillis)
    fun `test resolver runs on a given dispatcher`() = runBlockingInTestScope {

        val env = Env<Char, String, Char>(
            Initializer(""),
            CheckingResolver(coroutineDispatcher),
            ::messageAsCommand,
            coroutineDispatcher
        )

        factory(env)('a'..'d')
            .take('d' - 'a').collect()
    }

    @Test(timeout = TestTimeoutMillis)
    fun `test component throws exception given resolver throws exception`() =
        runBlockingInTestScope {

            val exception = RuntimeException("test exception")

            val env = Env<Char, String, Char>(
                Initializer(""),
                ThrowingResolver(exception),
                ::messageAsCommand,
                coroutineDispatcher
            )

            assertThrows<Throwable> {
                runBlocking {
                    factory(env)('a'..'d').collect()
                }
            }.shouldHaveMessage(exception.message!!)
        }

    @Test(timeout = TestTimeoutMillis)
    fun `test initializer runs on a given dispatcher`() = runBlockingInTestScope {

        val env = Env<Char, String, Char>(
            CheckingInitializer(CoroutineDispatcher),
            ::throwingResolver,
            { m, _ -> m.toString().noCommand() },
            CoroutineDispatcher
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
    }

    @Test(timeout = TestTimeoutMillis)
    fun `test updater runs on a given dispatcher`() = runBlockingInTestScope {

        val env = Env<Char, String, Char>(
            Initializer(""),
            ::throwingResolver,
            CheckingUpdater(Regex("$ThreadName @coroutine#\\d+")),
            computation = CoroutineDispatcher
        )

        factory(env)('a'..'d').take('d' - 'a').collect()
    }

}

@Suppress("UNUSED_PARAMETER")
private fun <T> noOpSink(t: T) = Unit

private fun CheckingInitializer(
    expectedDispatcher: CoroutineDispatcher,
): Initializer<String, Nothing> = {
    coroutineContext[ContinuationInterceptor] shouldBeSameInstanceAs expectedDispatcher
    Initial("", emptySet())
}

private fun CheckingResolver(
    expectedDispatcher: CoroutineDispatcher,
): Resolver<Any?, Nothing> = {
    coroutineContext[CoroutineDispatcher.Key] shouldBeSameInstanceAs expectedDispatcher
    emptySet()
}

fun <C> ThrowingResolver(
    th: Throwable,
): Resolver<C, Nothing> = { throw th }

fun ThrowingInitializer(
    th: Throwable,
): Initializer<Nothing, Nothing> = { throw th }

private fun currentThreadName(): String =
    Thread.currentThread().name

private fun <M, S> CheckingUpdater(
    expectedThreadGroup: Regex,
): Updater<M, S, Nothing> = { _, s ->

    val threadName = currentThreadName()

    withClue("Thread name should match '${expectedThreadGroup.pattern}' but was '$threadName'") {
        threadName.matches(expectedThreadGroup).shouldBeTrue()
    }
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
