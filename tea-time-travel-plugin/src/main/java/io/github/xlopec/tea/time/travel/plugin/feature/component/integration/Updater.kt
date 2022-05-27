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

package io.github.xlopec.tea.time.travel.plugin.feature.component.integration

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoApplyState
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.ComponentMessage
import io.github.xlopec.tea.time.travel.plugin.integration.warnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.detailedOutputEnabled
import io.github.xlopec.tea.time.travel.plugin.model.isStarted
import io.github.xlopec.tea.time.travel.plugin.model.removeSnapshots
import io.github.xlopec.tea.time.travel.plugin.model.serverSettings
import io.github.xlopec.tea.time.travel.plugin.model.snapshot
import io.github.xlopec.tea.time.travel.plugin.model.state
import io.github.xlopec.tea.time.travel.plugin.model.updateComponents
import io.github.xlopec.tea.time.travel.plugin.model.updateFilter
import io.github.xlopec.tea.time.travel.protocol.ComponentId

fun updateForUiMessage(
    message: ComponentMessage,
    state: State,
): Update<State, Command> =
    when {
        message is UpdateDebugSettings -> updateDebugSettings(message.isDetailedToStringEnabled, state)
        message is UpdateServerSettings && !state.isStarted -> updateServerSettings(message, state)
        message is RemoveSnapshots -> removeSnapshots(message.componentId, message.ids, state)
        message is RemoveAllSnapshots -> removeSnapshots(message.componentId, state)
        message is RemoveComponent -> removeComponent(message, state)
        message is ApplyMessage && state.server is Server -> applyMessage(message, state, state.server)
        message is ApplyState && state.server is Server -> applyState(message, state, state.server)
        message is UpdateFilter -> updateFilter(message, state)
        else -> warnUnacceptableMessage(message, state)
    }

private fun updateDebugSettings(
    isDetailedToStringEnabled: Boolean,
    state: State,
): Update<State, DoStoreSettings> =
    state.detailedOutputEnabled(isDetailedToStringEnabled) command { DoStoreSettings(settings) }

private fun updateServerSettings(
    message: UpdateServerSettings,
    state: State,
): Update<State, DoStoreSettings> {
    val settings = Settings.of(message.host, message.port, state.settings.isDetailedOutput)

    return state.serverSettings(settings) command { DoStoreSettings(settings) }
}

private fun applyState(
    message: ApplyState,
    state: State,
    server: Server,
): Update<State, Command> =
    state command DoApplyState(message.componentId, state.state(message), server)

private fun applyMessage(
    message: ApplyMessage,
    state: State,
    server: Server,
): Update<State, Command> {
    val m = state.messageFor(message) ?: return state.noCommand()

    return state command DoApplyMessage(message.componentId, m, server)
}

private fun removeSnapshots(
    componentId: ComponentId,
    ids: Set<SnapshotId>,
    state: State,
): Update<State, Nothing> =
    state.removeSnapshots(componentId, ids).noCommand()

private fun removeSnapshots(
    componentId: ComponentId,
    state: State,
): Update<State, Nothing> =
    state.removeSnapshots(componentId).noCommand()

private fun removeComponent(
    message: RemoveComponent,
    state: State,
): Update<State, Nothing> =
    state.updateComponents { mapping -> mapping.remove(message.componentId) }
        .noCommand()

private fun updateFilter(
    message: UpdateFilter,
    state: State,
): Update<State, Nothing> =
    state.updateFilter(message.id, message.input, message.ignoreCase, message.option).noCommand()

private fun State.state(
    message: ApplyState,
) = state(message.componentId, message.snapshotId)

private fun State.messageFor(
    message: ApplyMessage,
) = snapshot(message.componentId, message.snapshotId).message
