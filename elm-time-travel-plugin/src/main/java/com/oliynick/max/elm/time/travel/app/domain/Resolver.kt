package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.sideEffect
import com.oliynick.max.elm.time.travel.app.transport.EngineManager
import com.oliynick.max.elm.time.travel.protocol.ApplyCommands
import kotlinx.coroutines.channels.BroadcastChannel
import org.apache.commons.collections.set.ListOrderedSet
import java.io.File

data class Dependencies(
    val events: BroadcastChannel<PluginMessage>,
    val manager: EngineManager
)

suspend fun Dependencies.resolve(command: PluginCommand): Set<PluginMessage> {
    suspend fun resolve(): Set<PluginMessage> {
        return when (command) {
            is DoAddFiles -> command.effect { FilesUpdated(
                ListOrderedSet.decorate(to.toMutableList()).also { it.addAll(files) }.asList() as List<File>) }
            is DoRemoveFiles -> command.effect { FilesUpdated(ListOrderedSet.decorate(from.toMutableList()).also { it.removeAll(files) }.asList() as List<File>) }
            is DoStartServer -> command.sideEffect { manager.start(command.settings, events) }
            DoStopServer -> command.sideEffect { manager.stop() }
            is DoApplyCommands -> command.sideEffect { manager.outgoing.send(id to ApplyCommands(commands)) }
        }
    }

    return resolve()
}