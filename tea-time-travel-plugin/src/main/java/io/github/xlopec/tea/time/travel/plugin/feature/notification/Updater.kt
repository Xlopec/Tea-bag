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
import io.github.xlopec.tea.time.travel.plugin.Command
import io.github.xlopec.tea.time.travel.plugin.InternalException
import io.github.xlopec.tea.time.travel.plugin.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.PluginException
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStopServer
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.model.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.model.DebugState
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Settings
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.model.state.Server
import io.github.xlopec.tea.time.travel.plugin.model.state.Started
import io.github.xlopec.tea.time.travel.plugin.model.state.Starting
import io.github.xlopec.tea.time.travel.plugin.model.state.State
import io.github.xlopec.tea.time.travel.plugin.model.state.Stopped
import io.github.xlopec.tea.time.travel.plugin.model.state.appendSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.state.updateComponents
import io.github.xlopec.tea.time.travel.plugin.warnUnacceptableMessage
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun updateForNotification(
    message: NotificationMessage,
    state: State
): Update<State, Command> =
    when {
        message is NotifyStarted -> toStartedState(message.server, state.settings)
        message is NotifyStopped -> toStoppedState(state.settings)
        message is AppendSnapshot && state is Started -> appendSnapshot(message, state)
        message is StateApplied && state is Started -> applyState(message, state)
        message is ComponentAttached && state is Started -> attachComponent(message, state)
        message is ComponentImported && state is Started -> appendComponent(message, state)
        message is OperationException -> recoverFromException(message, state)
        else -> warnUnacceptableMessage(message, state)
    }

private fun appendComponent(
    message: ComponentImported,
    state: Started
): Update<State, Command> =
// overwrite any existing session for now
    // todo: show prompt dialog in future with options to merge, overwrite and cancel
    state.updateComponents { mapping ->
        mapping.put(message.sessionState.id, message.sessionState)
    }.noCommand()

private fun toStartedState(
    server: Server,
    settings: Settings
): Update<Started, Command> =
    Started(
        settings,
        DebugState(),
        server
    ).noCommand()

private fun toStoppedState(
    settings: Settings
): Update<Stopped, Command> =
    Stopped(settings).noCommand()

private fun appendSnapshot(
    message: AppendSnapshot,
    state: Started
): Update<State, Command> {

    val snapshot = message.toSnapshot()
    val updated = state.debugState.componentOrNew(message.componentId, message.newState)
        .appendSnapshot(snapshot, message.newState)

    return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
        .noCommand()
}

private fun attachComponent(
    message: ComponentAttached,
    state: Started
): Update<State, Command> {

    val id = message.componentId
    val currentState = message.state
    val componentState = state.debugState.componentOrNew(id, currentState)
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
    state: Started
): Update<State, Command> {

    val component = state.debugState.components[message.componentId] ?: return state.noCommand()
    val updated = component.copy(state = message.state)

    return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
        .noCommand()
}

private fun recoverFromException(
    message: OperationException,
    state: State
): Update<State, Command> =
    when {
        isFatalProblem(message.exception, message.operation) -> notifyDeveloperException(message.exception)
        message.operation is DoStartServer && state is Starting -> Stopped(state.settings) command DoNotifyOperationException(
            message
        )
        else -> state command DoNotifyOperationException(message)
    }

private fun isFatalProblem(
    th: PluginException,
    op: Command?
): Boolean =
    op is DoStopServer || op is DoStoreSettings || th is InternalException

private fun AppendSnapshot.toSnapshot() = OriginalSnapshot(meta, message, newState, commands)

private fun ComponentAttached.toSnapshot() = OriginalSnapshot(meta, null, state, commands)

private fun DebugState.componentOrNew(
    id: ComponentId,
    state: Value
) = components[id] ?: ComponentDebugState(id, state)

private fun DoNotifyOperationException(
    message: OperationException,
) = DoNotifyOperationException(message.exception, message.operation, message.description)

private fun notifyDeveloperException(cause: Throwable): Nothing =
    throw IllegalStateException(
        "Unexpected exception. Please, inform developers about the problem",
        cause
    )
