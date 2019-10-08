package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.sideEffect
import com.oliynick.max.elm.time.travel.app.transport.EngineManager
import kotlinx.coroutines.channels.Channel
import org.apache.commons.collections.set.ListOrderedSet
import java.io.File

data class Dependencies(
    val events: Channel<PluginMessage>,
    val manager: EngineManager
)

suspend fun Dependencies.resolve(command: PluginCommand): Set<PluginMessage> {
    suspend fun resolve(): Set<PluginMessage> {
        return when (command) {
            is DoAddFiles -> command.effect { Updated(
                ListOrderedSet.decorate(to.toMutableList()).also { it.addAll(files) }.asList() as List<File>) }
            is DoRemoveFiles -> command.effect { Updated(ListOrderedSet.decorate(from.toMutableList()).also { it.removeAll(files) }.asList() as List<File>) }
            is DoStartServer -> command.sideEffect { manager.start(command.settings, events) }
            DoStopServer -> command.sideEffect { manager.stop() }
        }
    }

    return resolve()
}