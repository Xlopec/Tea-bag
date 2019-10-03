package com.oliynick.max.elm.time.travel.app.editor

import com.intellij.openapi.vfs.VirtualFile
import com.oliynick.max.elm.core.component.effect
import org.apache.commons.collections.set.ListOrderedSet

sealed class PluginCommand

data class DoAddFiles(val files: List<VirtualFile>, val to: List<VirtualFile>) : PluginCommand()

data class DoRemoveFiles(val files: List<VirtualFile>, val from: List<VirtualFile>) : PluginCommand()

object DoStartServer : PluginCommand()

suspend fun resolve(command: PluginCommand): Set<PluginMessage> {
    suspend fun resolve(): Set<PluginMessage> {
        return when (command) {
            is DoAddFiles -> command.effect { Updated(ListOrderedSet.decorate(to.toMutableList()).also { it.addAll(files) }.asList() as List<VirtualFile>) }
            is DoRemoveFiles -> command.effect { Updated(ListOrderedSet.decorate(from.toMutableList()).also { it.removeAll(files) }.asList() as List<VirtualFile>) }
            DoStartServer -> TODO()
        }
    }

    return resolve()
}