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

import io.github.xlopec.tea.time.travel.plugin.data.SettingsGen
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.feature.notification.DoWarnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.isValid
import io.github.xlopec.tea.time.travel.plugin.model.value
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.properties.forAll
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class UpdateForServerMessageTest {

    @Test
    fun `test the result is calculated properly given plugin state is Stopped and message is StartServer`() =
        forAll(SettingsGen) { settings ->

            val stopped = State(settings)
            val (state, commands) = updateForServerMessage(StartServer, stopped)

            if (settings.host.isValid() && settings.port.isValid()) {
                state === stopped &&
                        commands == setOf(DoStartServer(ServerAddress(settings.host.value!!, settings.port.value!!)))
            } else {
                state === stopped && commands.isEmpty()
            }
        }

    @Test
    fun `test the result is calculated properly given plugin state is Started and message is StopServer`() =
        forAll(SettingsGen) { settings ->

            val pluginState = State(settings, server = StartedTestServerStub)
            val (state, commands) = updateForServerMessage(StopServer, pluginState)

            state == pluginState && commands == setOf(DoStopServer(pluginState.server as Server))
        }

    @Test
    fun `test when illegal combination of message and state warning command is returned`() {

        val initialState = State(ValidTestSettings)
        val (state, commands) = updateForServerMessage(StopServer, initialState)

        state shouldBeSameInstanceAs initialState
        commands.shouldContainExactly(DoWarnUnacceptableMessage(StopServer, initialState))
    }
}
