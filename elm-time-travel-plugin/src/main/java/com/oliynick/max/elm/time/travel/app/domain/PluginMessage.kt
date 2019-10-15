package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.io.File
import java.util.*

sealed class PluginMessage

data class AddFiles(val files: List<File>) : PluginMessage()

data class RemoveFiles(val files: List<File>) : PluginMessage()

data class UpdatePort(val port: UInt) : PluginMessage()

data class UpdateHost(val host: String) : PluginMessage()

data class NotifyMissingDependency(val exception: ClassNotFoundException) : PluginMessage()

data class NotifyOperationException(val exception: Throwable) : PluginMessage()

object NotifyStarted : PluginMessage()

object NotifyStopped : PluginMessage()

object StartServer : PluginMessage()

object StopServer : PluginMessage()

data class AppendSnapshot(val componentId: ComponentId, val message: Any, val oldState: Any, val newState: Any) : PluginMessage()

data class RemoveSnapshots(val componentId: ComponentId, val ids: Set<UUID>) : PluginMessage()

data class ReApplyCommands(val componentId: ComponentId, val commands: List<Any>) : PluginMessage()

data class ReApplyState(val componentId: ComponentId, val state: Any) : PluginMessage()

data class RemoveComponent(val componentId: ComponentId) : PluginMessage()