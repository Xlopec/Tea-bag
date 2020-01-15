package com.oliynick.max.elm.core.component

import com.oliynick.max.elm.core.loop.Component
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.yield
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LooperTest {

    @Test
    fun `test component emits a correct sequence of snapshots`() = runBlockingTest {

        val env = Env<String, String, String>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.noCommand() }
        )

        val component = Component(env)
        val messages = arrayOf("a", "b", "c")
        val snapshots = component(*messages).take(messages.size + 1).toList(ArrayList(messages.size + 1))

        snapshots shouldBe listOf(
            Initial("", emptySet()),
            Regular("a", "a", emptySet()),
            Regular("b", "b", emptySet()),
            Regular("c", "c", emptySet())
        )
    }

    @Test
    fun `test interceptor sees an original sequence of snapshots`() = runBlockingTest {

        val env = Env<String, String, String>(
            "",
            { c -> setOf(c) },
            { m, _ -> m.noCommand() }
        )

        val sink = mutableListOf<Snapshot<String, String, String>>()
        val component = Component(env) with { sink.add(it) }
        val messages = arrayOf("a", "b", "c")
        val snapshots = component(*messages).take(messages.size + 1).toList(ArrayList(messages.size + 1))

        sink shouldBe snapshots
    }

    @Test
    fun `test component's job gets canceled properly`() = runBlockingTest {

        val env = Env(
            "",
            ::foreverWaitingResolver,
            { m, _ -> m.command(m) }
        )

        val component = Component(env)
        val job = launch { component("a", "b", "c").toList(ArrayList()) }

        yield()
        job.cancel()

        job.isActive.shouldBeFalse()
        isActive.shouldBeTrue()
    }
}

private suspend fun foreverWaitingResolver(
    m: String
): Set<String> {

    delay(Long.MAX_VALUE)

    error("Improper cancellation, message=$m")
}
