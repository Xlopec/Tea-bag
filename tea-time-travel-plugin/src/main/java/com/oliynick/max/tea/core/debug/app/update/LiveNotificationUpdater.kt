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

package com.oliynick.max.tea.core.debug.app.update

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.app.command.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.message.*
import com.oliynick.max.tea.core.debug.app.resolve.InternalException
import com.oliynick.max.tea.core.debug.app.resolve.PluginException
import com.oliynick.max.tea.core.debug.app.state.*
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveNotificationUpdater : NotificationUpdater {

    override fun update(
        message: NotificationMessage,
        state: State
    ): UpdateWith<State, Command> =
        when {
            message is NotifyStarted -> toStartedState(message.server, state.settings)
            message is NotifyStopped -> toStoppedState(state.settings)
            message is AppendSnapshot && state is Started -> appendSnapshot(message, state)
            message is StateApplied && state is Started -> applyState(message, state)
            message is ComponentAttached && state is Started -> attachComponent(message, state)
            message is NotifyOperationException -> recoverFromException(message.exception, message.operation, state)
            else -> warnUnacceptableMessage(message, state)
        }

    fun toStartedState(
        server: Server,
        settings: Settings
    ): UpdateWith<Started, Command> =
        Started(
            settings,
            DebugState(),
            server
        ).noCommand()

    fun toStoppedState(
        settings: Settings
    ): UpdateWith<Stopped, Command> =
        Stopped(settings).noCommand()

    fun appendSnapshot(
        message: AppendSnapshot,
        state: Started
    ): UpdateWith<State, Command> {

        val snapshot = message.toSnapshot()
        val updated = state.debugState.componentOrNew(message.componentId, message.newState)
            .appendSnapshot(snapshot, message.newState)

        return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
            .noCommand()
    }

    fun attachComponent(
        message: ComponentAttached,
        state: Started
    ): UpdateWith<State, Command> {

        val id = message.componentId
        val currentState = message.state
        val componentState = state.debugState.componentOrNew(id, currentState)
            .appendSnapshot(message.toSnapshot(), currentState)

        return state.updateComponents { mapping -> mapping.put(componentState.id, componentState) } command DoNotifyComponentAttached(id)
    }

    fun applyState(
        message: StateApplied,
        state: Started
    ): UpdateWith<State, Command> {

        val component = state.debugState.components[message.componentId] ?: return state.noCommand()
        val updated = component.copy(state = message.state)

        return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
            .noCommand()
    }

    fun recoverFromException(
        th: PluginException,
        op: Command?,
        state: State
    ): UpdateWith<State, Command> =
        when {
            isFatalProblem(th, op) -> notifyDeveloperException(th)
            op is DoStartServer && state is Starting -> Stopped(state.settings) command DoNotifyOperationException(th, op)
            else -> state command DoNotifyOperationException(th, op)
        }

    fun warnUnacceptableMessage(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        state command DoWarnUnacceptableMessage(message, state)

    fun isFatalProblem(
        th: PluginException,
        op: Command?
    ): Boolean =
        op is DoStopServer || op is DoStoreSettings || th is InternalException

    fun AppendSnapshot.toSnapshot() = OriginalSnapshot(meta, message, newState, commands)

    fun ComponentAttached.toSnapshot() = OriginalSnapshot(meta, null, state, commands)

    fun DebugState.componentOrNew(
        id: ComponentId,
        state: Value
    ) = components[id] ?: ComponentDebugState(id, state)

    fun notifyDeveloperException(cause: Throwable): Nothing =
        throw IllegalStateException(
            "Unexpected exception. Please, inform developers about the problem",
            cause
        )

}

