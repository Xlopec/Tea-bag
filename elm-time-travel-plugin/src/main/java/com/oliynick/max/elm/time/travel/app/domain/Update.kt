package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.app.misc.mergeWith
import com.oliynick.max.elm.time.travel.protocol.ComponentId

internal fun update(message: PluginMessage, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    return when (message) {
        is AddFiles -> addFiles(message, state)
        is RemoveFiles -> removeFiles(message, state)
        StartServer -> startServer(state)
        NotifyStarted -> Started(state.settings, DebugState()).noCommand()
        NotifyStopped -> Stopped(state.settings).noCommand()
        StopServer -> stopServer(state)
        is AppendCommands -> appendCommands(message, state)
        is ReApplyCommands -> applyCommands(message, state)
        is RemoveCommands -> removeCommands(message, state)
        is UpdatePort -> updateServerSettings(state.settings.serverSettings.copy(port = message.port), state)
        is UpdateHost -> updateServerSettings(state.settings.serverSettings.copy(host = message.host), state)
        is NotifyMissingDependency -> notifyMissingDependency(message, state)
        is NotifyOperationException -> TODO()
        is AppendSnapshot -> appendSnapshot(message, state)
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

private fun appendCommands(message: AppendCommands, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    if (state is Started) {

        val updated = state.debugState[message.componentId].appendCommands(message.commands)

        return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
            .noCommand()
    }

    return state.noCommand()
}

private fun appendSnapshot(message: AppendSnapshot, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    if (state is Started) {

        val updated = state.debugState[message.componentId].appendState(message.message, message.newState)

        return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
            .noCommand()
    }

    return state.noCommand()
}

private fun applyCommands(message: ReApplyCommands, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    require(state is Started) { "Not a started state, was $state" }

    return state.command(DoApplyCommands(message.componentId, message.commands))
}

private fun removeCommands(message: RemoveCommands, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    require(state is Started) { "Not a started state, was $state" }

    val updated = state.debugState[message.componentId].removeCommands(message.indexes)

    return state.copy(debugState = state.debugState.copy(components = state.debugState.components + updated.asPair()))
        .noCommand()
}

private fun notifyMissingDependency(message: NotifyMissingDependency, state: PluginState): UpdateWith<PluginState, DoNotifyMissingDependency> {
    return state.command(DoNotifyMissingDependency(message.exception))
}

private operator fun DebugState.get(id: ComponentId) = components[id] ?: ComponentDebugState(id)

private fun ComponentDebugState.appendCommands(actual: List<Any>) =
    copy(commands = commands + actual.map { RemoteObject(it.traverse(), it) })

private fun ComponentDebugState.appendState(message: Any, state: Any) =
    copy(commands = commands + message.toRemoteRepresentation(), states = states + state.toRemoteRepresentation())

private fun Any.toRemoteRepresentation() = RemoteObject(traverse(), this)

private fun ComponentDebugState.removeCommands(indexes: IntArray) = copy(commands = commands.filterIndexed { index, _ -> !indexes.contains(index) })

private fun ComponentDebugState.asPair() = id to this

private fun notifyIllegalMessage(message: String): Nothing = throw IllegalArgumentException(message)