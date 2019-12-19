@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.app.domain.resolver

import com.intellij.ide.util.PropertiesComponent
import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.sideEffect
import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.app.storage.paths
import com.oliynick.max.elm.time.travel.app.storage.serverSettings
import com.oliynick.max.elm.time.travel.app.transport.GSON
import com.oliynick.max.elm.time.travel.app.transport.ServerHandler
import com.oliynick.max.elm.time.travel.app.transport.asJson
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import protocol.ApplyMessage
import protocol.ApplyState


fun HasServerService(server: ServerHandler = ServerHandler()) =
    object : HasServerService {
        override val server: ServerHandler = server
    }

fun HasSystemProperties(properties: PropertiesComponent) =
    object : HasSystemProperties {
        override val properties: PropertiesComponent = properties
    }

fun HasChannels(
    events: Channel<PluginMessage> = Channel(),
    exceptions: BroadcastChannel<DoNotifyOperationException> = BroadcastChannel(1),
    notifications: BroadcastChannel<NotificationMessage> = BroadcastChannel(1)
) =
    HasChannels(Channels(
        events,
        exceptions,
        notifications))

fun HasChannels(channels: Channels) =
    object : HasChannels {
        override val channels = channels
    }

data class Channels(
    val events: Channel<PluginMessage>,
    val exceptions: BroadcastChannel<DoNotifyOperationException>,
    val notifications: BroadcastChannel<NotificationMessage>
)

interface HasChannels {
    val channels: Channels
}

interface HasServerService {
    val server: ServerHandler
}

interface HasSystemProperties {
    val properties: PropertiesComponent
}

fun <Env> LiveAppResolver() where Env : HasChannels,
                                  Env : HasServerService,
                                  Env : HasSystemProperties = object :
    LiveAppResolver<Env> {}

interface LiveAppResolver<Env> :
    AppResolver<Env> where Env : HasChannels,
                           Env : HasServerService,
                           Env : HasSystemProperties {

    override suspend fun Env.resolve(command: PluginCommand): Set<PluginMessage> {

        suspend fun resolve(): Set<PluginMessage> {
            return when (command) {
                is StoreFiles -> command sideEffect { properties.paths = files }
                is StoreServerSettings -> command sideEffect {
                    properties.serverSettings = serverSettings
                }
                is DoStartServer -> command effect {
                    server.start(
                        command.settings,
                        channels.events
                    ); NotifyStarted
                }
                DoStopServer -> command effect { server.stop(); NotifyStopped }
                is DoApplyCommand -> command.sideEffect {
                    server(
                        id,
                        // todo refactor
                        ApplyMessage(GSON.asJson(command.command))
                    )
                }
                is DoNotifyOperationException -> command.sideEffect {
                    channels.exceptions.send(
                        command
                    )
                }
                is DoApplyState -> command.effect {
                    server(
                        id,
                        // todo refactor
                        ApplyState(GSON.asJson(state))
                    )

                    StateReApplied(
                        id,
                        state
                    )
                }
            }
        }

        return runCatching {
            resolve().also { messages ->
                channels.notifications.send(messages.notifications())
            }
        }.getOrElse { th ->
            setOf(
                NotifyOperationException(
                    th,
                    command
                )
            )
        }
    }

    fun Iterable<PluginMessage>.notifications() = filterIsInstance<NotificationMessage>()

    suspend fun BroadcastChannel<NotificationMessage>.send(messages: Iterable<NotificationMessage>) =
        messages.forEach { notification -> send(notification) }

}
