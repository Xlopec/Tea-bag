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

package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStopServer
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.InternalException
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.PluginException
import io.github.xlopec.tea.time.travel.plugin.integration.warnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.model.appendSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.updateComponents
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun updateForNotification(
    message: NotificationMessage,
    state: State,
): Update<State, Command> =
    when (message) {
        is NotifyStarted -> toStartedState(message.server, state)
        is NotifyStopped -> toStoppedState(state)
        is AppendSnapshot -> appendSnapshot(message, state)
        is StateApplied -> applyState(message, state)
        is ComponentAttached -> attachComponent(message, state)
        is ComponentImported -> appendComponent(message, state)
        is OperationException -> recoverFromException(message, state)
        else -> warnUnacceptableMessage(message, state)
    }

private fun appendComponent(
    message: ComponentImported,
    state: State,
): Update<State, Command> =
// overwrite any existing session for now
    // todo: show prompt dialog in future with options to merge, overwrite and cancel
    state.updateComponents { mapping ->
        mapping.put(message.sessionState.id, message.sessionState)
    }.noCommand()

private fun toStartedState(
    server: Server,
    state: State,
) = state.copy(server = server).noCommand()

private fun toStoppedState(
    state: State,
) = state.copy(server = null).noCommand()

private fun appendSnapshot(
    message: AppendSnapshot,
    state: State,
): Update<State, Command> {

    val snapshot = message.toSnapshot()
    val updated = state.debugger.componentOrNew(message.componentId, message.newState)
        .appendSnapshot(snapshot, message.newState)

    return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
        .noCommand()
}

private fun attachComponent(
    message: ComponentAttached,
    state: State,
): Update<State, Command> {

    val id = message.componentId
    val currentState = message.state
    val componentState = state.debugger.componentOrNew(id, currentState)
        .appendSnapshot(message.toSnapshot(), currentState)

    return state.updateComponents { mapping ->
        mapping.put(
            componentState.id,
            componentState
        )
    } command DoNotifyComponentAttached(id)
}

private fun applyState(
    message: StateApplied,
    state: State,
): Update<State, Command> {

    val component = state.debugger.components[message.componentId] ?: return state.noCommand()
    val updated = component.copy(state = message.state)

    return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
        .noCommand()
}

private fun recoverFromException(
    message: OperationException,
    state: State,
): Update<State, Command> =
    when {
        isFatalProblem(message.exception, message.operation) -> notifyDeveloperException(message.exception)
        message.operation is DoStartServer -> state.copy(server = null) command DoNotifyOperationException(
            message
        )
        else -> state command DoNotifyOperationException(message)
    }

private fun isFatalProblem(
    th: PluginException,
    op: Command?,
): Boolean =
    op is DoStopServer || op is DoStoreSettings || th is InternalException

private fun AppendSnapshot.toSnapshot() = OriginalSnapshot(meta, message, newState, commands)

private fun ComponentAttached.toSnapshot() = OriginalSnapshot(meta, null, state, commands)

private fun Debugger.componentOrNew(
    id: ComponentId,
    state: Value,
) = components[id] ?: DebuggableComponent(id, state)

private fun DoNotifyOperationException(
    message: OperationException,
) = DoNotifyOperationException(message.exception, message.operation, message.description)

private fun notifyDeveloperException(cause: Throwable): Nothing =
    throw IllegalStateException(
        "Unexpected exception. Please, inform developers about the problem",
        cause
    )
