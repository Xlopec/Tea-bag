package com.oliynick.max.elm.time.travel.app.plugin

import com.oliynick.max.elm.time.travel.app.LevelNode
import java.io.File

data class ServerSettings(val port: UInt = 8080U)

data class Settings(
    val serverSettings: ServerSettings,
    val classFiles: List<File> = listOf() // implies ordered set
)

data class DebugState(val commandNodes: List<LevelNode> = emptyList())

sealed class PluginState {
    abstract val settings: Settings
}

data class Stopped(override val settings: Settings) : PluginState() {
    inline val canStart: Boolean get() = settings.classFiles.isNotEmpty()
}

data class Starting(override val settings: Settings) : PluginState()

data class Started(override val settings: Settings, val debugState: DebugState) : PluginState()

data class Stopping(override val settings: Settings) : PluginState()