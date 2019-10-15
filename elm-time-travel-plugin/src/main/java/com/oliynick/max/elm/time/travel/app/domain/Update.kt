package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.app.misc.mergeWith
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.time.LocalDateTime
import java.util.*

internal fun update(message: PluginMessage, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    return when (message) {
        is AddFiles -> addFiles(message, state)
        is RemoveFiles -> removeFiles(message, state)
        StartServer -> startServer(state)
        NotifyStarted -> Started(state.settings, DebugState()).noCommand()
        NotifyStopped -> Stopped(state.settings).noCommand()
        StopServer -> stopServer(state)
        is ReApplyCommands -> reApplyCommands(message, state)
        is RemoveSnapshots -> removeSnapshots(message, state)
        is UpdatePort -> updateServerSettings(state.settings.serverSettings.copy(port = message.port), state)
        is UpdateHost -> updateServerSettings(state.settings.serverSettings.copy(host = message.host), state)
        is NotifyMissingDependency -> notifyMissingDependency(message, state)
        is NotifyOperationException -> TODO()
        is AppendSnapshot -> appendSnapshot(message, state)
        is RemoveComponent -> removeComponent(message, state)
        is ReApplyState -> reApplyState(message, state)
    }
}

private fun addFiles(message: AddFiles, state: PluginState): UpdateWith<Stopped, StoreFiles> {
    require(state is Stopped) { "Not a stopped state, was $state" }

    val updatedPaths = state.settings.classFiles.mergeWith(message.files)

    return state.copy(settings = state.settings.copy(classFiles = updatedPaths)).command(StoreFiles(updatedPaths))
}

private fun removeFiles(message: RemoveFiles, state: PluginState): UpdateWith<Stopped, StoreFiles> {
    require(state is Stopped) { "Not a stopped state, was $state" }

    val updatedPaths = state.settings.classFiles - message.files

    return state.copy(settings = state.settings.copy(classFiles = updatedPaths)).command(StoreFiles(updatedPaths))
}

private fun updateServerSettings(serverSettings: ServerSettings, state: PluginState): UpdateWith<Stopped, StoreServerSettings> {
    require(state is Stopped) { "Not a stopped state, was $state" }

    if (state.settings.serverSettings == serverSettings) {
        return state.noCommand()
    }

    return state.copy(settings = state.settings.copy(serverSettings = serverSettings)).command(StoreServerSettings(serverSettings))
}

private fun startServer(state: PluginState): UpdateWith<Starting, DoStartServer> {
    return if (state is Stopped) Starting(state.settings).command(DoStartServer(state.settings))
    else notifyIllegalMessage("can't start server when state is $state")
}

private fun stopServer(state: PluginState): UpdateWith<Stopping, DoStopServer> {
    return if (state is Started) Stopping(state.settings).command(DoStopServer)
    else notifyIllegalMessage("can't stop server when state is $state")
}

private fun appendSnapshot(message: AppendSnapshot, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    if (state is Started) {

        val updated = state.debugState[message.componentId].appendSnapshot(message.toSnapshot())

        return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
            .noCommand()
    }

    return state.noCommand()
}

private fun reApplyState(message: ReApplyState, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    require(state is Started) { "Not a started state, was $state" }

    return state.command(DoApplyState(message.componentId, message.state))
}

private fun reApplyCommands(message: ReApplyCommands, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    require(state is Started) { "Not a started state, was $state" }

    return state.command(DoApplyCommands(message.componentId, message.commands))
}

private fun removeSnapshots(message: RemoveSnapshots, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    require(state is Started) { "Not a started state, was $state" }

    val updated = state.debugState[message.componentId].removeSnapshots(message.ids)

    return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
        .noCommand()
}

private fun removeComponent(message: RemoveComponent, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    require(state is Started) { "Not a started state, was $state" }

    return state.copy(debugState = state.debugState.copy(components = state.debugState.components - message.componentId))
        .noCommand()
}

private fun notifyMissingDependency(message: NotifyMissingDependency, state: PluginState): UpdateWith<PluginState, DoNotifyMissingDependency> {
    return state.command(DoNotifyMissingDependency(message.exception))
}

private operator fun DebugState.get(id: ComponentId) = components[id] ?: ComponentDebugState(id)

private fun ComponentDebugState.appendSnapshot(snapshot: Snapshot) =
    copy(snapshots = snapshots + snapshot)

private fun Any.toRemoteRepresentation() = RemoteObject(traverse(), this)

private fun ComponentDebugState.removeSnapshots(ids: Set<UUID>): ComponentDebugState {
    return copy(snapshots = snapshots.filter { snapshot -> !ids.contains(snapshot.id) })
}

private fun ComponentDebugState.asPair() = id to this

private fun AppendSnapshot.toSnapshot(): Snapshot {
    return Snapshot(UUID.randomUUID(), LocalDateTime.now(),
        message.toRemoteRepresentation(), newState.toRemoteRepresentation())
}

private fun notifyIllegalMessage(message: String): Nothing = throw IllegalArgumentException(message)