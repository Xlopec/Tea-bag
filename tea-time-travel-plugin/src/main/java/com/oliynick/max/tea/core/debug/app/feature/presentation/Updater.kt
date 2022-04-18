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

package com.oliynick.max.tea.core.debug.app.feature.presentation

import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.UIMessage
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.feature.server.DoApplyMessage
import com.oliynick.max.tea.core.debug.app.feature.server.DoApplyState
import com.oliynick.max.tea.core.debug.app.feature.storage.DoStoreSettings
import com.oliynick.max.tea.core.debug.app.state.Started
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.app.state.Stopped
import com.oliynick.max.tea.core.debug.app.state.removeSnapshots
import com.oliynick.max.tea.core.debug.app.state.snapshot
import com.oliynick.max.tea.core.debug.app.state.state
import com.oliynick.max.tea.core.debug.app.state.updateComponents
import com.oliynick.max.tea.core.debug.app.state.updateFilter
import com.oliynick.max.tea.core.debug.app.state.updateServerSettings
import com.oliynick.max.tea.core.debug.app.state.updateSettings
import com.oliynick.max.tea.core.debug.app.warnUnacceptableMessage
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand

fun updateForUiMessage(
    message: UIMessage,
    state: State
): Update<State, Command> =
    when {
        message is UpdateDebugSettings -> updateDebugSettings(message.isDetailedToStringEnabled, state)
        message is UpdateServerSettings && state is Stopped -> updateServerSettings(message, state)
        message is RemoveSnapshots && state is Started -> removeSnapshots(message.componentId, message.ids, state)
        message is RemoveAllSnapshots && state is Started -> removeSnapshots(message.componentId, state)
        message is RemoveComponent && state is Started -> removeComponent(message, state)
        message is ApplyMessage && state is Started -> applyMessage(message, state)
        message is ApplyState && state is Started -> applyState(message, state)
        message is UpdateFilter && state is Started -> updateFilter(message, state)
        else -> warnUnacceptableMessage(message, state)
    }

private fun updateDebugSettings(
    isDetailedToStringEnabled: Boolean,
    state: State
): Update<State, DoStoreSettings> =
    state.updateSettings { copy(isDetailedOutput = isDetailedToStringEnabled) } command { DoStoreSettings(settings) }

private fun updateServerSettings(
    message: UpdateServerSettings,
    state: State
): Update<State, DoStoreSettings> {
    val settings = Settings.of(message.host, message.port, state.settings.isDetailedOutput)

    return state.updateServerSettings(settings) command { DoStoreSettings(settings) }
}

private fun applyState(
    message: ApplyState,
    state: Started
): Update<State, Command> =
    state command DoApplyState(message.componentId, state.state(message), state.server)

private fun applyMessage(
    message: ApplyMessage,
    state: Started
): Update<State, Command> {
    val m = state.messageFor(message) ?: return state.noCommand()

    return state command DoApplyMessage(message.componentId, m, state.server)
}


private fun removeSnapshots(
    componentId: ComponentId,
    ids: Set<SnapshotId>,
    state: Started
): Update<State, Nothing> =
    state.removeSnapshots(componentId, ids).noCommand()

private fun removeSnapshots(
    componentId: ComponentId,
    state: Started
): Update<State, Nothing> =
    state.removeSnapshots(componentId).noCommand()

private fun removeComponent(
    message: RemoveComponent,
    state: Started
): Update<State, Nothing> =
    state.updateComponents { mapping -> mapping.remove(message.componentId) }
        .noCommand()

private fun updateFilter(
    message: UpdateFilter,
    state: Started
): Update<State, Nothing> =
    state.updateFilter(message.id, message.input, message.ignoreCase, message.option).noCommand()

private fun Started.state(
    message: ApplyState
) = state(message.componentId, message.snapshotId)

private fun Started.messageFor(
    message: ApplyMessage
) = snapshot(message.componentId, message.snapshotId).message
