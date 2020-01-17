package com.oliynick.max.elm.core.component

import com.oliynick.max.elm.core.loop.ComponentFock
import core.component.BasicComponentTest
import kotlinx.coroutines.delay
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ZaLooperTest : BasicComponentTest({ env -> ComponentFock(env) }) {

    /*@Test
    fun `test component emits a correct sequence of snapshots`() = runBlocking {

        val env = Env<Char, Char, String>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.toString().noCommand() }
        )

        val component = ComponentFock(env)
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList(ArrayList(messages.size + 1))

        snapshots shouldContainExactly listOf(
            Initial("", emptySet()),
            Regular('a', "a", emptySet()),
            Regular('b', "b", emptySet()),
            Regular('c', "c", emptySet())
        )
    }

    @Test
    fun `test component emits a correct sequence of snapshots if initial commands were present`() =
        runBlocking {

            val env = Env<Char, Char, String>(
                InitializerLegacy("", 'a', 'b', 'c'),
                { c -> setOf(c) },
                { m, _ -> m.toString().noCommand() }
            )

            val component = ComponentFock(env)
            val messages = arrayOf('d', 'e', 'f')
            val snapshots =
                component(*messages).take(3 + messages.size + 1)
                    .toList(ArrayList(3 + messages.size + 1))

            snapshots shouldContainExactly listOf(
                Initial("", setOf('a', 'b', 'c')),
                Regular('a', "a", emptySet()),
                Regular('b', "b", emptySet()),
                Regular('c', "c", emptySet()),
                Regular('d', "d", emptySet()),
                Regular('e', "e", emptySet()),
                Regular('f', "f", emptySet())
            )
        }

    @Test
    fun `test component emits a correct sequence of snapshots if we have recursive calculations`() =
        runBlocking {

            val env = Env<Char, Char, String>(
                "",
                { ch ->
                    if (ch == 'a') setOf(
                        ch.inc(),// only this message should be consumed
                        ch.inc().inc(),
                        ch.inc().inc().inc()
                    ) else emptySet()
                },
                { m, str -> (str + m).command(m) }
            )

            val component = ComponentFock(env)
            val snapshots = component('a').take(3).toCollection(ArrayList())

            @Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
            snapshots shouldBe listOf<Snapshot<Char, String, Char>>(
                Initial("", emptySet()),
                Regular('a', "a", setOf('a')),
                Regular('b', "ab", setOf('b'))
            )
        }

    @Test
    fun `test component emits a correct sequence of snapshots if update returns set of messages`() =
        runBlocking {

            val env = Env<Char, Char, Pair<String, Boolean>>(
                "" to true,
                { ch -> setOf(ch) },
                { m, (str, flag) ->
                    (str + m to false).command(
                        if (flag) setOf(
                            'b',
                            'c'
                        ) else emptySet()
                    )
                }
            )

            val component = ComponentFock(env)
            val snapshots = component('a').take(3).toCollection(ArrayList())

            @Suppress("RemoveExplicitTypeArguments")// helps to track down types when refactoring
            snapshots shouldBe listOf<Snapshot<Char, Pair<String, Boolean>, Char>>(
                Initial("" to true, emptySet()),
                Regular('a', "a" to false, setOf('b', 'c')),
                Regular('b', "ab" to false, emptySet())
            )
        }

    @Test
    fun `test interceptor sees an original sequence of snapshots`() = runBlocking {

        val env = Env<Char, Char, String>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.toString().noCommand() }
        )

        val sink = mutableListOf<Snapshot<Char, String, Char>>()
        val component = ComponentFock(env) with { sink.add(it) }
        val messages = arrayOf('a', 'b', 'c')
        val snapshots =
            component(*messages).take(messages.size + 1).toList(ArrayList(messages.size + 1))

        sink shouldContainExactly snapshots
    }

    @Test
    fun `test component's snapshots shared among consumers`() = runBlocking {

        val env = Env<Char, Char, String>(
            "",
            { ch ->
                if (ch == 'a') setOf(
                    ch.inc(),// only this message should be consumed
                    ch.inc().inc(),
                    ch.inc().inc().inc()
                ) else emptySet()
            },
            { m, str -> (str + m).command(m) }
        )

        val take = 3
        val component = ComponentFock(env)
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
            Regular('a', "a", setOf('a')),
            Regular('b', "ab", setOf('b'))
        )

        snapshots1Deferred.await().asClue { it shouldContainExactly expected }
        snapshots2Deferred.await().asClue { it shouldContainExactly expected }
    }

    @Test
    fun `test component gets initialized only once if we have multiple consumers`() =
        runBlocking {

            val countingInitializer = object {

                val invocations = atomic(0)

                fun initializer(): InitializerLegacy<String, Nothing> = {
                    invocations.incrementAndGet()
                    yield()
                    "bar" to emptySet()
                }
            }

            val env = Env<Char, Char, String>(
                countingInitializer.initializer(),
                ::throwingResolver,
                { _, s -> s.noCommand() }
            )

            val component = ComponentFock(env)

            countingInitializer.invocations.value shouldBe 0

            val coroutines = 100
            val jobs = (0 until coroutines).map { launch { component('a').first() } }
                .toCollection(ArrayList(coroutines))

            jobs.joinAll()

            countingInitializer.invocations.value shouldBe 1
        }

    @Test
    fun `test component's job gets canceled properly`() = runBlocking {

        val env = Env<Char, Char, String>(
            "",
            ::foreverWaitingResolver,
            { m, _ -> m.toString().command(m) }
        )

        val component = ComponentFock(env)
        val job = launch { component('a', 'b', 'c').toList(ArrayList()) }

        yield()
        job.cancel()

        job.isActive.shouldBeFalse()
        isActive.shouldBeTrue()
    }

    @Test
    fun `test component doesn't block if serves multiple message sources`() =
        runBlockingInTestScope {

            val env = Env<Char, Char, String>(
                "",
                ::throwingResolver,
                { m, _ -> m.toString().noCommand() }
            )

            val range = 'a'..'h'
            val component = ComponentFock(env)

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
                listOf(Initial("", emptySet<Char>())) + range.map { ch ->
                    Regular(
                        ch,
                        ch.toString(),
                        emptySet()
                    )
                }

            snapshots1Deferred.await().asClue { it shouldContainExactly expected }
            snapshots2Deferred.await().asClue { it shouldContainExactly expected }
        }*/

}

private suspend fun <T> foreverWaitingResolver(
    m: T
): Nothing {

    delay(Long.MAX_VALUE)

    error("Improper cancellation, message=$m")
}
