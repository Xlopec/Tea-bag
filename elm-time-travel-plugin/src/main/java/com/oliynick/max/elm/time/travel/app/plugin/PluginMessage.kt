package com.oliynick.max.elm.time.travel.app.plugin

import java.io.File

sealed class PluginMessage

data class AddFiles(val files: List<File>) : PluginMessage()

data class Updated(val files: List<File>) : PluginMessage()

data class RemoveFiles(val files: List<File>) : PluginMessage()

object NotifyStarting : PluginMessage()

object NotifyStarted : PluginMessage()

object NotifyStopping : PluginMessage()

object NotifyStopped : PluginMessage()

object StartServer : PluginMessage()

object StopServer : PluginMessage()

data class AppendCommands(val commands: List<Any>) : PluginMessage()