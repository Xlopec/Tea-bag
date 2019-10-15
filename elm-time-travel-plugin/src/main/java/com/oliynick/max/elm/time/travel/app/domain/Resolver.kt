package com.oliynick.max.elm.time.travel.app.domain

import com.intellij.ide.util.PropertiesComponent
import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.sideEffect
import com.oliynick.max.elm.time.travel.app.storage.paths
import com.oliynick.max.elm.time.travel.app.storage.serverSettings
import com.oliynick.max.elm.time.travel.app.transport.EngineManager
import com.oliynick.max.elm.time.travel.protocol.ApplyMessage
import com.oliynick.max.elm.time.travel.protocol.ApplyState
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

data class Dependencies(
    val events: Channel<PluginMessage>,
    val manager: EngineManager,
    val properties: PropertiesComponent
) {
    val commands: BroadcastChannel<PluginCommand> = BroadcastChannel(1)
}

suspend fun Dependencies.resolve(command: PluginCommand): Set<PluginMessage> {
    suspend fun resolve(): Set<PluginMessage> {
        return when (command) {
            is StoreFiles -> command.sideEffect { properties.paths = files }
            is StoreServerSettings -> command.sideEffect { properties.serverSettings = serverSettings }
            is DoStartServer -> command.effect { manager.start(command.settings, events); NotifyStarted }
            DoStopServer -> command.effect { manager.stop(); NotifyStopped }
            is DoApplyCommands -> command.sideEffect { manager.outgoing.send(id to ApplyMessage(commands)) }
            is DoNotifyMissingDependency -> command.sideEffect {  }
            is DoApplyState -> command.effect { manager.outgoing.send(id to ApplyState(state)); StateReApplied(id, state) }
        }
    }

    commands.offer(command)
    return resolve()
}