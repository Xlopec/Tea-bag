package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.io.File

sealed class PluginCommand

data class StoreFiles(val files: List<File>) : PluginCommand()

data class StoreServerSettings(val serverSettings: ServerSettings) : PluginCommand()

data class DoStartServer(val settings: Settings) : PluginCommand()

object DoStopServer : PluginCommand()

data class DoApplyCommands(val id: ComponentId, val commands: List<Any>) : PluginCommand()

data class DoApplyState(val id: ComponentId, val state: Any) : PluginCommand()

data class DoNotifyMissingDependency(val exception: ClassNotFoundException) : PluginCommand()

