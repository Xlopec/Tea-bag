@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.Initializer
import core.component.BasicComponentTest
import core.misc.messageAsCommandUpdate
import core.misc.throwingResolver
import core.misc.throwingUpdater
import core.scope.runBlockingInNewScope
import core.scope.runBlockingInTestScope
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldThrowAnyUnit
import io.kotlintest.shouldThrowExactly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest : BasicComponentTest(::ComponentFactory) {

    private companion object {

        fun ComponentFactory(
            @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
            env: Env<Char, String, Char>
        ): Component<Char, String, Char> = Component(env)
    }

    @Test
    fun `test if initializer fails with exception it gets propagated`() = runBlocking {

        val component = Component<String, String, String>(
            { throw RuntimeException("hello") },
            ::throwingResolver,
            ::throwingUpdater
        )

        shouldThrowExactly<RuntimeException> { component("").collect() }
            .shouldHaveMessage("hello")
    }

    @Test
    fun `test if resolver fails with exception it gets propagated`() = runBlockingInNewScope {

        val component = Component(
            Initializer("", "a"),
            ::throwingResolver,
            ::messageAsCommandUpdate
        )

        shouldThrowAnyUnit { component("").collect() }
    }

    @Test
    fun `test resolver tasks runs in parallel`() = runBlockingInTestScope {

        pauseDispatcher {
            val env = Env<Char, String, Char>(
                "",
                { cmd -> delay(cmd); emptySet() },
                { m, _ -> m.toString().command(if (m == 'a') (m until m + 6).toSet() else emptySet()) }
            )

            val component = factory(env)
            val result = component('a').take(2).toCollection(ArrayList(2))

            println(result)

            result.map { it.commands }.flatten() shouldContainExactly listOf('a', 'b', 'c', 'd', 'e', 'f')

        }
    }

}

suspend fun delay(
    ch: Char
) = delay(((ch.toInt() % 'a'.toInt()) * 100L + 500L).also { println("$ch wait for $it") })
