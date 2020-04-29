@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.Initializer
import core.component.BasicComponentTest
import core.misc.*
import core.scope.runBlockingInNewScope
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldThrowAnyUnit
import io.kotlintest.shouldThrowExactly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
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
                ::messageAsCommand
        )

        shouldThrowAnyUnit { component("").collect() }
    }

}
