package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.io.File

data class ServerSettings(val host: String = "0.0.0.0", val port: UInt = 8080U)

data class Settings(
    val serverSettings: ServerSettings,
    val classFiles: List<File> = listOf() // implies ordering
)

data class DebugState(val components: Map<ComponentId, ComponentDebugState> = emptyMap())

data class RemoteObject(val representation: TypeNode, val value: Any)

data class ComponentDebugState(val id: ComponentId,
                               val commands: List<RemoteObject> = emptyList(),
                               val states: List<RemoteObject> = emptyList())

sealed class PluginState {
    abstract val settings: Settings
}

data class Stopped(override val settings: Settings) : PluginState() {
    inline val canStart: Boolean get() = settings.classFiles.isNotEmpty()
}

data class Starting(override val settings: Settings) : PluginState()

data class Started(override val settings: Settings, val debugState: DebugState) : PluginState()

data class Stopping(override val settings: Settings) : PluginState()