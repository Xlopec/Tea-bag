package com.oliynick.max.elm.time.travel.app.plugin

import java.io.File

sealed class PluginCommand

data class DoAddFiles(val files: List<File>, val to: List<File>) : PluginCommand()

data class DoRemoveFiles(val files: List<File>, val from: List<File>) : PluginCommand()

data class DoStartServer(val settings: Settings) : PluginCommand()

object DoStopServer : PluginCommand()

