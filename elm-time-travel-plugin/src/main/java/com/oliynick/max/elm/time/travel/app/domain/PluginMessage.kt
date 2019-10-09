package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.io.File

sealed class PluginMessage

data class AddFiles(val files: List<File>) : PluginMessage()

data class FilesUpdated(val files: List<File>) : PluginMessage()

data class RemoveFiles(val files: List<File>) : PluginMessage()

object NotifyStarting : PluginMessage()

object NotifyStarted : PluginMessage()

object NotifyStopping : PluginMessage()

object NotifyStopped : PluginMessage()

object StartServer : PluginMessage()

object StopServer : PluginMessage()

data class AppendCommands(val componentId: ComponentId, val commands: List<Any>) : PluginMessage()

data class RemoveCommands(val componentId: ComponentId, val indexes: IntArray) : PluginMessage()

data class ReApplyCommands(val componentId: ComponentId, val commands: List<Any>) : PluginMessage()