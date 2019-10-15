package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.io.File
import java.time.LocalDateTime
import java.util.*

data class ServerSettings(val host: String = "0.0.0.0", val port: UInt = 8080U)

data class Settings(
    val serverSettings: ServerSettings,
    val classFiles: List<File> = listOf() // implies ordering
)

data class DebugState(val components: Map<ComponentId, ComponentDebugState> = emptyMap())

data class RemoteObject(val representation: TypeNode, val value: Any)

data class Snapshot(val id: UUID, val timestamp: LocalDateTime, val message: RemoteObject, val state: RemoteObject)

data class ComponentDebugState(val id: ComponentId,
                               val currentState: RemoteObject,
                               val snapshots: List<Snapshot> = emptyList())

sealed class PluginState {
    abstract val settings: Settings
}

data class Stopped(override val settings: Settings) : PluginState() {
    inline val canStart: Boolean get() = settings.classFiles.isNotEmpty()
}

data class Starting(override val settings: Settings) : PluginState()

data class Started(override val settings: Settings, val debugState: DebugState) : PluginState()

data class Stopping(override val settings: Settings) : PluginState()