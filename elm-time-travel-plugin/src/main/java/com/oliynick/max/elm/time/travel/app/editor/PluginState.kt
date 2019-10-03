package com.oliynick.max.elm.time.travel.app.editor

import com.intellij.openapi.vfs.VirtualFile
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

data class ServerSettings(val port: UInt = 8080U)

data class Settings(
    val serverSettings: ServerSettings,
    val classFiles: List<VirtualFile> = listOf() // implies ordered set
)

sealed class PluginState {
    abstract val settings: Settings
}

data class Stopped(override val settings: Settings) : PluginState() {
    inline val canStart: Boolean get() = settings.classFiles.isNotEmpty()
}

data class Starting(override val settings: Settings) : PluginState()

data class Running(override val settings: Settings) : PluginState()

data class Stopping(override val settings: Settings) : PluginState()

internal fun update(message: PluginMessage, state: PluginState): UpdateWith<PluginState, PluginCommand> {
    return when (message) {
        is AddFiles -> addFiles(message, state)
        is RemoveFiles -> removeFiles(message, state)
        StartServer -> startServer(state)
        is Updated -> swapFiles(message, state)
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
    return if (state is Stopped) Starting(state.settings).command(DoStartServer)
    else notifyIllegalMessage("can't start server when state is $state")
}

private fun swapFiles(message: Updated, state: PluginState): UpdateWith<Stopped, PluginCommand> {
    return if (state is Stopped) Stopped(state.settings.copy(classFiles = message.files)).noCommand()
    else notifyIllegalMessage("can't add files when state is $state")
}

private fun notifyIllegalMessage(message: String): Nothing = throw IllegalArgumentException(message)