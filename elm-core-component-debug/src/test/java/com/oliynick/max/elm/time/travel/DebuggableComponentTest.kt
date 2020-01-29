package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.time.travel.component.Component
import com.oliynick.max.elm.time.travel.component.connectionFailureMessage
import com.oliynick.max.elm.time.travel.exception.ConnectException
import com.oliynick.max.elm.time.travel.session.WebSocketSession
import core.component.BasicComponentTest
import core.misc.throwingResolver
import io.kotlintest.fail
import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.matchers.numerics.shouldNotBeExactly
import io.kotlintest.matchers.throwable.shouldHaveCauseOfType
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactlyUnit
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.ActionApplied
import protocol.JsonTree
import protocol.NotifyComponentAttached
import protocol.NotifyComponentSnapshot

@RunWith(JUnit4::class)
class DebuggableComponentTest : BasicComponentTest({ env ->
    Component(
        TestEnv(
            env = env
        )
    )
}) {

    private val testEnv = Env(
        "",
        ::throwingResolver,
        { m: String, _ -> m.noCommand<String, String>() }
    )

    @Test
    @Ignore
    fun `test debuggable component throws expected exception when it can't connect to a server`() = runBlocking {

        val component = Component(
            TestEnv(
                env = testEnv,
                serverSettings = TestServerSettings(sessionBuilder = ::WebSocketSession)
            )
        )

        val th = shouldThrowExactlyUnit<ConnectException> {
            component("a").collect()
        }

        th shouldHaveMessage connectionFailureMessage(TestServerSettings<String, String>())
        th.shouldHaveCauseOfType<java.net.ConnectException>()
    }

    @Test
    fun `test debuggable component sends the same sequence of events as the original component`() = runBlocking {

        val testSession = TestDebugSession<String, String>()
        val component = Component(
            TestEnv(
                env = testEnv,
                serverSettings = TestServerSettings(
                    sessionBuilder = { _, block -> testSession.apply { block() } }
                )
            )
        )
        val messages = arrayOf("a", "b", "c")
        val snapshots = component(*messages).take(messages.size + 1).toCollection(ArrayList(messages.size + 1))

        snapshots shouldBe listOf(
            Initial("", emptySet<String>()),
            Regular("a", "a", emptySet()),
            Regular("b", "b", emptySet()),
            Regular("c", "c", emptySet())
        )

        testSession.packets.forEachIndexed { index, elem ->

            elem.componentId shouldBe testComponentId

            when (val payload = elem.payload) {
                is NotifyComponentSnapshot -> {
                    index shouldNotBeExactly 0
                    fromJson(payload.message) shouldBe messages[index - 1]
                    fromJson(payload.newState) shouldBe messages[index - 1]
                }

                is NotifyComponentAttached -> {
                    index shouldBeExactly 0
                    fromJson(payload.state) shouldBe ""
                }

                is ActionApplied -> fail("Shouldn't get here. Index=$index, elem=$elem")
            }
        }
    }

}

private fun fromJson(
    tree: JsonTree
) = testSerializer.fromJsonTree(tree, String::class.java)

