/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.feature.notification.DoWarnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

val TestHost = Host.of("localhost")!!
val TestPort = Port(123)

private val TestHosts = listOf(
    Invalid("", ""),
    Invalid("some", "message"),
    Invalid("", ""),
    Valid(
        TestHost.value,
        TestHost
    ),
    Invalid("some", "message"),
    Valid(
        "www.google.com",
        Host.of("www.google.com")!!
    )
)

private val TestPorts = listOf(
    Invalid("", ""),
    Invalid("some", "message"),
    Invalid("", ""),
    Valid(
        TestPort.value.toString(),
        TestPort
    ),
    Invalid("some", "message"),
    Valid(
        "100",
        Port(100)
    )
)

private val TestSettings = TestHosts.map { host ->
    TestPorts.map { port -> Settings(host, port, false) }
}.flatten()

@RunWith(JUnit4::class)
internal class UpdateForServerMessageTest {

    @Test
    fun `test the result is calculated properly given plugin state is Stopped and message is StartServer`() {
        TestSettings.forEach { settings ->
            val stopped = State(settings)
            val (state, commands) = stopped.onUpdateForServerMessage(StartServer)

            assertTrue("test failed for settings $settings") {
                if (settings.host.isValid() && settings.port.isValid()) {
                    state === stopped &&
                            commands == setOf(
                        DoStartServer(
                            ServerAddress(
                                settings.host.value!!,
                                settings.port.value!!
                            )
                        )
                    )
                } else {
                    state === stopped && commands.isEmpty()
                }
            }
        }
    }

    @Test
    fun `test the result is calculated properly given plugin state is Started and message is StopServer`() {
        TestSettings.forEach { settings ->

            assertTrue("test failed for settings $settings") {
                val pluginState = State(settings, server = StartedTestServerStub)
                val (state, commands) = pluginState.onUpdateForServerMessage(StopServer)

                state == pluginState && commands == setOf(DoStopServer(pluginState.server as Server))
            }
        }
    }

    @Test
    fun `test when illegal combination of message and state warning command is returned`() {

        val initialState = State(ValidTestSettings)
        val (state, commands) = initialState.onUpdateForServerMessage(StopServer)

        assertSame(initialState, state)
        assertEquals(setOf(DoWarnUnacceptableMessage(StopServer, initialState)), commands)
    }
}
