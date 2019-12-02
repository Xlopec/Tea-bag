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

interface UiUpdater {
    fun update(message: UIMessage, state: PluginState): UpdateWith<PluginState, PluginCommand>
}

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveUiUpdater : UiUpdater {
    override fun update(
        message: UIMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        updateForUser(message, state)

    fun updateForUser(
        message: UIMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is UpdatePort && state is Stopped -> updateServerSettings(
                state.settings.serverSettings.copy(
                    port = message.port
                ), state
            )
            message is UpdateHost && state is Stopped -> updateServerSettings(
                state.settings.serverSettings.copy(
                    host = message.host
                ), state
            )
            message === StartServer && state is Stopped -> startServer(state)
            message === StopServer && state is Started -> stopServer(state)
            message is RemoveSnapshots && state is Started -> removeSnapshots(message, state)
            message is ReApplyCommands && state is Started -> reApplyCommands(message, state)
            message is ReApplyState && state is Started -> reApplyState(message, state)
            message is RemoveComponent && state is Started -> removeComponent(message, state)
            else -> notifyIllegalMessage(message, state)
        }

    fun updateServerSettings(
        serverSettings: ServerSettings,
        state: Stopped
    ): UpdateWith<Stopped, StoreServerSettings> {
        //todo consider implementing generic memoization?
        if (state.settings.serverSettings == serverSettings) {
            return state.noCommand()
        }

        return state.copy(settings = state.settings.copy(serverSettings = serverSettings))
            .command(StoreServerSettings(serverSettings))
    }

    fun startServer(state: Stopped): UpdateWith<Starting, DoStartServer> {
        return Starting(state.settings)
            .command(DoStartServer(state.settings))
    }

    fun stopServer(state: Started): UpdateWith<Stopping, DoStopServer> {
        return Stopping(state.settings).command(DoStopServer)
    }

    fun reApplyState(
        message: ReApplyState,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {
        return state.command(DoApplyState(message.componentId, message.state))
    }

    fun reApplyCommands(
        message: ReApplyCommands,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {
        return state.command(DoApplyCommand(message.componentId, message.command))
    }

    fun removeSnapshots(
        message: RemoveSnapshots,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {
        val component = state.debugState.components[message.componentId]
            ?: throw IllegalArgumentException("Unknown component ${message.componentId}")
        val updated = component.removeSnapshots(message.ids)
        // todo use Started.updateComponents() function
        return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
            .noCommand()
    }

    fun removeComponent(
        message: RemoveComponent,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {
        return state.copy(debugState = state.debugState.copy(components = state.debugState.components - message.componentId))
            .noCommand()
    }

}
