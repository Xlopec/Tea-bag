package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.protocol.ComponentId

internal fun update(message: PluginMessage, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    return when (message) {
        is AddFiles -> addFiles(message, state)
        is RemoveFiles -> removeFiles(message, state)
        StartServer -> startServer(state)
        is FilesUpdated -> swapFiles(message, state)
        NotifyStarting -> Starting(state.settings).noCommand()
        NotifyStarted -> Started(state.settings, DebugState()).noCommand()
        NotifyStopping -> Stopping(state.settings).noCommand()
        NotifyStopped -> Stopped(state.settings).noCommand()
        StopServer -> stopServer(state)
        is AppendCommands -> appendCommands(message, state)
        is ReApplyCommands -> applyCommands(message, state)
        is RemoveCommands -> removeCommands(message, state)
    }
}

private fun addFiles(message: AddFiles, state: PluginState): UpdateWith<Stopped, DoAddFiles> {
    return if (state is Stopped) state.command(DoAddFiles(message.files, state.settings.classFiles))
    else notifyIllegalMessage("can't add files when state is $state")
}

private fun removeFiles(message: RemoveFiles, state: PluginState): UpdateWith<Stopped, DoRemoveFiles> {
    return if (state is Stopped) state.command(DoRemoveFiles(message.files, state.settings.classFiles))
    else notifyIllegalMessage("can't remove files when state is $state")
}

private fun startServer(state: PluginState): UpdateWith<Starting, DoStartServer> {
    return if (state is Stopped) Starting(state.settings).command(DoStartServer(state.settings))
    else notifyIllegalMessage("can't start server when state is $state")
}

private fun stopServer(state: PluginState): UpdateWith<Stopping, DoStopServer> {
    return if (state is Started) Stopping(state.settings).command(DoStopServer)
    else notifyIllegalMessage("can't stop server when state is $state")
}

private fun swapFiles(message: FilesUpdated, state: PluginState): UpdateWith<Stopped, PluginCommand> {
    return if (state is Stopped) Stopped(state.settings.copy(classFiles = message.files)).noCommand()
    else notifyIllegalMessage("can't add files when state is $state")
}

private fun appendCommands(message: AppendCommands, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    if (state is Started) {

        val updated = state.debugState[message.componentId].appendCommands(message.commands)

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

private operator fun DebugState.get(id: ComponentId) = components[id] ?: ComponentDebugState(id)

private fun ComponentDebugState.appendCommands(actual: List<Any>) =
    copy(commands = commands + actual.map { RemoteCommand(it.traverse(), it) })

private fun ComponentDebugState.removeCommands(indexes: IntArray) = copy(commands = commands.filterIndexed { index, _ -> !indexes.contains(index) })

private fun ComponentDebugState.asPair() = id to this

private fun notifyIllegalMessage(message: String): Nothing = throw IllegalArgumentException(message)