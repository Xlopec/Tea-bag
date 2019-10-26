/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

internal fun updateForUser(message: UIMessage, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    return when (message) {
        is UpdatePort -> updateServerSettings(state.settings.serverSettings.copy(port = message.port), toExpected(state))
        is UpdateHost -> updateServerSettings(state.settings.serverSettings.copy(host = message.host), toExpected(state))
        StartServer -> startServer(toExpected(state))
        StopServer -> stopServer(toExpected(state))
        is RemoveSnapshots -> removeSnapshots(message, toExpected(state))
        is ReApplyCommands -> reApplyCommands(message, toExpected(state))
        is ReApplyState -> reApplyState(message, toExpected(state))
        is RemoveComponent -> removeComponent(message, toExpected(state))
    }
}

private fun updateServerSettings(serverSettings: ServerSettings, state: Stopped): UpdateWith<Stopped, StoreServerSettings> {
    if (state.settings.serverSettings == serverSettings) {
        return state.noCommand()
    }

    return state.copy(settings = state.settings.copy(serverSettings = serverSettings)).command(StoreServerSettings(serverSettings))
}

private fun startServer(state: Stopped): UpdateWith<Starting, DoStartServer> {
    return Starting(state.settings)
        .command(DoStartServer(state.settings))
}

private fun stopServer(state: Started): UpdateWith<Stopping, DoStopServer> {
    return Stopping(state.settings).command(DoStopServer)
}

private fun reApplyState(message: ReApplyState, state: Started): UpdateWith<PluginState, PluginCommand> {
    return state.command(DoApplyState(message.componentId, message.state))
}

private fun reApplyCommands(message: ReApplyCommands, state: Started): UpdateWith<PluginState, PluginCommand> {
    return state.command(DoApplyCommand(message.componentId, message.command))
}

private fun removeSnapshots(message: RemoveSnapshots, state: Started): UpdateWith<PluginState, PluginCommand> {
    val component = state.debugState.components[message.componentId] ?: throw IllegalArgumentException("Unknown component ${message.componentId}")
    val updated = component.removeSnapshots(message.ids)

    return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
        .noCommand()
}

private fun removeComponent(message: RemoveComponent, state: Started): UpdateWith<PluginState, PluginCommand> {
    return state.copy(debugState = state.debugState.copy(components = state.debugState.components - message.componentId))
        .noCommand()
}