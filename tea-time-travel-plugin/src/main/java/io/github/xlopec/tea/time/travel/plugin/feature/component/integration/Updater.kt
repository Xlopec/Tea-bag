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
import io.github.xlopec.tea.time.travel.plugin.feature.settings.*
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.ComponentMessage
import io.github.xlopec.tea.time.travel.plugin.integration.onUnhandledMessage
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId

fun State.onUpdateForComponentMessage(
    message: ComponentMessage,
): Update<State, Command> =
    when {
        message is UpdateDebugSettings -> onUpdateDebugSettings(
            message.isDetailedToStringEnabled,
            message.clearSnapshotsOnComponentAttach,
            message.maxRetainedSnapshots,
        )
        message is UpdateServerSettings && !isStarted -> onUpdateServerSettings(message)
        message is RemoveSnapshots -> onRemoveSnapshots(message.componentId, message.ids)
        message is RemoveAllSnapshots -> onRemoveSnapshots(message.componentId)
        message is RemoveComponent -> onRemoveComponent(message)
        message is SelectComponent -> onSelectComponent(message.id)
        message is ApplyMessage && server is Server -> onApplyMessage(message, server)
        message is ApplyState && server is Server -> onApplyState(message, server)
        message is UpdateFilter -> onUpdateFilter(message)
        else -> onUnhandledMessage(message)
    }

private fun State.onUpdateDebugSettings(
    isDetailedToStringEnabled: Boolean,
    clearSnapshotsOnComponentAttach: Boolean,
    maxRetainedSnapshots: PositiveNumber,
): Update<State, DoStoreSettings> =
    settings(settings.update(isDetailedToStringEnabled, clearSnapshotsOnComponentAttach, maxRetainedSnapshots)) command {
        DoStoreSettings(settings)
    }

private fun State.onUpdateServerSettings(
    message: UpdateServerSettings,
): Update<State, DoStoreSettings> =
    settings(settings.update(message.host, message.port)) command { DoStoreSettings(settings) }

private fun State.onApplyState(
    message: ApplyState,
    server: Server,
): Update<State, Command> =
    this command DoApplyState(message.componentId, state(message), server)

private fun State.onApplyMessage(
    message: ApplyMessage,
    server: Server,
): Update<State, Command> {
    val m = messageFor(message) ?: return noCommand()

    return this command DoApplyMessage(message.componentId, m, server)
}

private fun State.onRemoveSnapshots(
    id: ComponentId,
    ids: Set<SnapshotId>,
): Update<State, Nothing> =
    debugger(debugger.removeSnapshots(id, ids)).noCommand()

private fun State.onRemoveSnapshots(
    id: ComponentId,
): Update<State, Nothing> =
    debugger(debugger.removeSnapshots(id)).noCommand()

private fun State.onRemoveComponent(
    message: RemoveComponent,
): Update<State, Nothing> = debugger(debugger.removeComponent(message.id)).noCommand()

private fun State.onSelectComponent(
    id: ComponentId
) = debugger(debugger.selectComponent(id)).noCommand()

private fun State.onUpdateFilter(
    message: UpdateFilter,
): Update<State, Nothing> =
    debugger(debugger.updateFilter(message.id, message.input, message.ignoreCase, message.option)).noCommand()

private fun State.state(
    message: ApplyState,
) = state(message.componentId, message.snapshotId)

private fun State.messageFor(
    message: ApplyMessage,
) = snapshot(message.componentId, message.snapshotId).message

private fun Settings.update(
    hostInput: String?,
    portInput: String?,
): Settings = copy(host = ValidatedHost(hostInput), port = ValidatedPort(portInput))

private fun Settings.update(
    isDetailedOutput: Boolean = this.isDetailedOutput,
    clearSnapshotsOnAttach: Boolean = this.clearSnapshotsOnAttach,
    maxRetainedSnapshots: PositiveNumber = this.maxRetainedSnapshots,
): Settings = copy(
    isDetailedOutput = isDetailedOutput,
    clearSnapshotsOnAttach = clearSnapshotsOnAttach,
    maxRetainedSnapshots = maxRetainedSnapshots
)
