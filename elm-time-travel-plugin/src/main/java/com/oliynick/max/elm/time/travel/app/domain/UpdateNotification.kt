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
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.time.LocalDateTime
import java.util.*

internal fun updateForNotification(message: NotificationMessage, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    return when(message) {
        NotifyStarted -> Started(state.settings, DebugState()).noCommand()
        is NotifyMissingDependency -> notifyMissingDependency(message, state)
        NotifyStopped -> Stopped(state.settings).noCommand()
        is AppendSnapshot -> appendSnapshot(message, state)
        is StateReApplied -> reApplyState(message, toExpected(state))
        is ComponentAttached -> attachComponent(message, state)
        is NotifyOperationException -> TODO()
    }
}

private fun appendSnapshot(message: AppendSnapshot, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    if (state is Started) {

        val snapshot = message.toSnapshot()
        val updated = state.debugState.componentOrNew(message.componentId, snapshot.state).appendSnapshot(snapshot)

        return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
            .noCommand()
    }

    return state.noCommand()
}

private fun attachComponent(message: ComponentAttached, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    if (state is Started) {

        val id = message.componentId
        val currentState = message.state.toRemoteRepresentation()
        val componentState =  state.debugState.components[id]?.copy(currentState = currentState) ?: ComponentDebugState(id, currentState)

        return state.copy(debugState = state.debugState.copy(components = state.debugState.components + componentState.asPair()))
            .noCommand()
    }

    return state.noCommand()
}

private fun reApplyState(message: StateReApplied, state: Started): UpdateWith<PluginState, PluginCommand> {
    val component = state.debugState.components[message.componentId] ?: return state.noCommand()
    val updated = component.copy(currentState = message.state.toRemoteRepresentation())

    return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
        .noCommand()
}

private fun notifyMissingDependency(message: NotifyMissingDependency, state: PluginState): UpdateWith<PluginState, DoNotifyMissingDependency> {
    return state.command(DoNotifyMissingDependency(message.exception))
}

private fun Any.toRemoteRepresentation() = RemoteObject(traverse(), this)

private fun AppendSnapshot.toSnapshot(): Snapshot {
    return Snapshot(
        UUID.randomUUID(), LocalDateTime.now(),
        message.toRemoteRepresentation(), newState.toRemoteRepresentation()
    )
}

private fun DebugState.componentOrNew(id: ComponentId, state: RemoteObject) = components[id] ?: ComponentDebugState(id, state)