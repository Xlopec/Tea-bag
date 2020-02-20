package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Initializer
import core.component.BasicComponentTest
import core.misc.messageAsCommandUpdate
import core.misc.throwingResolver
import core.misc.throwingUpdater
import core.scope.runBlockingInNewScope
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldThrowAnyUnit
import io.kotlintest.shouldThrowExactly
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest : BasicComponentTest({ env -> Component(env) }) {

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

}
