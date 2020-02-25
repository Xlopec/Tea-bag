package com.oliynick.max.tea.core.debug

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.component.invoke
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.component.Component
import com.oliynick.max.tea.core.debug.exception.ConnectException
import com.oliynick.max.tea.core.debug.session.WebSocketSession
import core.component.BasicComponentTest
import core.misc.throwingResolver
import io.kotlintest.fail
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.matchers.numerics.shouldNotBeExactly
import io.kotlintest.matchers.throwable.shouldHaveCauseOfType
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactlyUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.ActionApplied
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
            Regular("a", emptySet(), "", "a"),
            Regular("b", emptySet(), "a", "b"),
            Regular("c", emptySet(), "b", "c")
        )

        testSession.packets.forEachIndexed { index, elem ->

            elem.componentId shouldBe testComponentId

            when (val payload = elem.payload) {
                is NotifyComponentSnapshot<JsonElement> -> {
                    index shouldNotBeExactly 0
                    fromJson(payload.message) shouldBe messages[index - 1]
                    fromJson(payload.newState) shouldBe messages[index - 1]
                }

                is NotifyComponentAttached<JsonElement> -> {
                    index shouldBeExactly 0
                    fromJson(payload.state) shouldBe ""
                }

                is ActionApplied -> fail("Shouldn't get here. Index=$index, elem=$elem")
            }
        }
    }

    @Test
    fun `test debuggable component processes server snapshots properly`() = runBlocking {

        val testSession = TestDebugSession<String, String>(states = flowOf("a"))
        val component = Component(
            TestEnv(
                env = testEnv.copy(initializer = { delay(Long.MAX_VALUE); error("shouldn't get here") }),
                serverSettings = TestServerSettings(
                    sessionBuilder = { _, block -> testSession.apply { block() } }
                )
            )
        )

        val snapshots = component("b")
            .take(2)
            .toCollection(ArrayList(2))

        snapshots shouldContainExactly listOf(
            Initial("a", emptySet()),
            Regular("b", emptySet(), "a", "b")
        )
    }

}

private fun fromJson(
    tree: JsonElement
) = testSerializer.fromJsonTree(tree, String::class.java)

