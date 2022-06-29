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
import io.github.xlopec.tea.time.travel.plugin.integration.*
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.model.appendSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.updateComponents
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun State.updateForNotificationMessage(
    message: NotificationMessage,
): Update<State, Command> =
    when (message) {
        is ServerStarted -> onStarted(message.server)
        is ServerStopped -> onStopped()
        is AppendSnapshot -> onAppendSnapshot(message)
        is StateDeployed -> onStateDeployed(message)
        is ComponentAttached -> onComponentAttached(message)
        is ComponentImported -> onComponentImported(message)
        is OperationException -> onOperationException(message)
        else -> onUnhandledMessage(message)
    }

private fun State.onComponentImported(
    message: ComponentImported,
): Update<State, Command> =
// overwrite any existing session for now
    // todo: show prompt dialog in future with options to merge, overwrite and cancel
    updateComponents { mapping ->
        mapping.put(message.sessionState.id, message.sessionState)
    }.noCommand()

private fun State.onStarted(
    server: Server,
) = copy(server = server).noCommand()

private fun State.onStopped() = copy(server = null).noCommand()

private fun State.onAppendSnapshot(
    message: AppendSnapshot,
): Update<State, Command> {

    val snapshot = message.toSnapshot()
    val updated = debugger.componentOrNew(message.componentId, message.newState)
        .appendSnapshot(snapshot, message.newState)

    return updateComponents { mapping -> mapping.put(updated.id, updated) }
        .noCommand()
}

private fun State.onComponentAttached(
    message: ComponentAttached,
): Update<State, Command> {

    val id = message.componentId
    val currentState = message.state
    val componentState = debugger.componentOrNew(id, currentState)
        .appendSnapshot(message.toSnapshot(), currentState)

    return updateComponents { mapping ->
        mapping.put(
            componentState.id,
            componentState
        )
    } command DoNotifyComponentAttached(id)
}

private fun State.onStateDeployed(
    message: StateDeployed,
): Update<State, Command> {

    val component = debugger.components[message.componentId] ?: return noCommand()
    val updated = component.copy(state = message.state)

    return updateComponents { mapping -> mapping.put(updated.id, updated) }
        .noCommand()
}

private fun State.onOperationException(
    message: OperationException,
): Update<State, Command> =
    when {
        isFatalProblem(message.exception, message.operation) -> notifyDeveloperException(message.exception)
        isManipulateServerException(message) -> onManipulateServerException(message)
        else -> onNonFatalOperationException(message)
    }

private fun isManipulateServerException(
    message: OperationException
) = message.operation is DoStartServer || message.operation is DoStopServer

private fun State.onManipulateServerException(
    message: OperationException
) = copy(server = null) command DoNotifyOperationException(message)

private fun State.onNonFatalOperationException(
    message: OperationException
) = this command DoNotifyOperationException(message)

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
