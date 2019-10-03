package com.oliynick.max.elm.time.travel.app.editor

import com.intellij.openapi.vfs.VirtualFile

sealed class PluginMessage

data class AddFiles(val files: List<VirtualFile>) : PluginMessage()

data class Updated(val files: List<VirtualFile>) : PluginMessage()

data class RemoveFiles(val files: List<VirtualFile>) : PluginMessage()

object StartServer : PluginMessage()