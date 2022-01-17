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

package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.protocol.ComponentId

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveUiUpdater : UiUpdater {

    override fun update(
        message: UIMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is UpdateDebugSettings -> updateDebugSettings(message.isDetailedToStringEnabled, state)
            message is UpdateServerSettings && state is Stopped -> updateServerSettings(message, state)
            message === StartServer && state is Stopped -> startServer(state)
            message === StopServer && state is Started -> stopServer(state)
            message is RemoveSnapshots && state is Started -> removeSnapshots(message.componentId, message.ids, state)
            message is RemoveAllSnapshots && state is Started -> removeSnapshots(message.componentId, state)
            message is RemoveComponent && state is Started -> removeComponent(message, state)
            message is ApplyMessage && state is Started -> applyMessage(message, state)
            message is ApplyState && state is Started -> applyState(message, state)
            message is UpdateFilter && state is Started -> updateFilter(message, state)
            else -> warnUnacceptableMessage(message, state)
        }

    fun updateDebugSettings(
        isDetailedToStringEnabled: Boolean,
        state: PluginState
    ): UpdateWith<PluginState, DoStoreSettings> =
        state.updateSettings { copy(isDetailedOutput = isDetailedToStringEnabled) } command { DoStoreSettings(settings) }

    fun updateServerSettings(
        message: UpdateServerSettings,
        state: PluginState
    ): UpdateWith<PluginState, DoStoreSettings> {
        val settings = Settings.of(message.host, message.port, state.settings.isDetailedOutput)

        return state.updateServerSettings(settings) command { DoStoreSettings(settings) }
    }

    fun startServer(
        state: Stopped
    ): UpdateWith<PluginState, PluginCommand> {

        val host = state.settings.host
        val port = state.settings.port

        return if (host is Valid && port is Valid) {
            Starting(state.settings) command DoStartServer(ServerAddress(host.t, port.t))
        } else state.noCommand()
    }

    fun stopServer(
        state: Started
    ): UpdateWith<Stopping, DoStopServer> =
        Stopping(state.settings) command DoStopServer(state.server)

    fun applyState(
        message: ApplyState,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoApplyState(message.componentId, state.state(message), state.server)

    fun applyMessage(
        message: ApplyMessage,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {
        val m = state.messageFor(message) ?: return state.noCommand()

        return state command DoApplyMessage(message.componentId, m, state.server)
    }


    fun removeSnapshots(
        componentId: ComponentId,
        ids: Set<SnapshotId>,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.removeSnapshots(componentId, ids).noCommand()

    fun removeSnapshots(
        componentId: ComponentId,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.removeSnapshots(componentId).noCommand()

    fun removeComponent(
        message: RemoveComponent,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.updateComponents { mapping -> mapping.remove(message.componentId) }
            .noCommand()

    fun updateFilter(
        message: UpdateFilter,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.updateFilter(message.id, message.input, message.ignoreCase, message.option).noCommand()

    fun Started.state(
        message: ApplyState
    ) = state(message.componentId, message.snapshotId)

    fun Started.messageFor(
        message: ApplyMessage
    ) = snapshot(message.componentId, message.snapshotId).message

}
