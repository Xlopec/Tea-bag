/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.transport

import com.oliynick.max.tea.core.debug.app.component.cms.AppendSnapshot
import com.oliynick.max.tea.core.debug.app.component.cms.ComponentAttached
import com.oliynick.max.tea.core.debug.app.component.cms.NotifyOperationException
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.SnapshotMeta
import com.oliynick.max.tea.core.debug.app.transport.serialization.GSON
import com.oliynick.max.tea.core.debug.app.transport.serialization.toValue
import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.gson.GsonNotifyComponentAttached
import com.oliynick.max.tea.core.debug.gson.GsonNotifyComponentSnapshot
import com.oliynick.max.tea.core.debug.gson.GsonNotifyServer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.NotifyClient
import com.oliynick.max.tea.core.debug.protocol.NotifyServer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.event.Level
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class ServerImpl private constructor(
    val address: ServerAddress,
    private val events: BroadcastChannel<PluginMessage>,
    private val calls: BroadcastChannel<RemoteCallArgs>
) : ApplicationEngine by Server(address, events, calls), Server {

    companion object {

        fun newInstance(
            address: ServerAddress,
            events: BroadcastChannel<PluginMessage>
        ): ServerImpl = ServerImpl(address, events, BroadcastChannel(1))

    }

    override suspend operator fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    ) = withContext(Dispatchers.IO) {
        calls.send(RemoteCallArgs(UUID.randomUUID(), component, message))
    }

    override suspend fun stop() =
        withContext(Dispatchers.IO) {
            stop(1, 1)
        }

}

private data class RemoteCallArgs(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)

private fun Server(
    address: ServerAddress,
    events: BroadcastChannel<PluginMessage>,
    calls: BroadcastChannel<RemoteCallArgs>
): NettyApplicationEngine =
    embeddedServer(
            Netty,
            host = address.host.value,
            port = address.port.value
    ) {

        install(CallLogging) {
            level = Level.INFO
            filter { call -> call.request.path().startsWith("/") }
        }

        install(ConditionalHeaders)
        install(DataConversion)
        install(DefaultHeaders) { header("X-Engine", "Ktor") }

        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(10)
            timeout = Duration.ofSeconds(5)
        }

        configureWebSocketRouting(calls.asFlow(), events)
    }

private fun Application.configureWebSocketRouting(
    calls: Flow<RemoteCallArgs>,
    events: BroadcastChannel<PluginMessage>
) {
    routing {

        webSocket("/") {

            launch {
                installPacketSender(calls, outgoing)
            }

            installPacketReceiver(events, incoming.consumeAsFlow().filterIsInstance())
        }
    }
}

private suspend fun installPacketSender(
    calls: Flow<RemoteCallArgs>,
    outgoing: SendChannel<Frame>
) = calls.collect { (callId, componentId, message) ->

    val json = GSON.toJson(NotifyClient(callId, componentId, message))

    outgoing.send(Frame.Text(json))
}

private suspend fun installPacketReceiver(
    events: BroadcastChannel<PluginMessage>,
    incoming: Flow<Frame.Text>
) {
    incoming.collect { frame -> processPacket(frame, events) }
}

private suspend fun processPacket(
    frame: Frame.Text,
    events: BroadcastChannel<PluginMessage>
) =
    withContext(Dispatchers.IO) {
        try {
            val json = frame.readText()
            val packet = GSON.fromJson<GsonNotifyServer>(json, NotifyServer::class.java)

            when (val message = packet.payload) {
                is GsonNotifyComponentSnapshot -> events.send(
                        message.toNotification(
                                packet.componentId,
                                SnapshotMeta(SnapshotId(UUID.randomUUID()), LocalDateTime.now())
                        )
                )
                is GsonNotifyComponentAttached -> events.send(message.toNotification(packet.componentId))
            }

        } catch (e: Throwable) {
            events.send(NotifyOperationException(e))
        }
    }

private fun GsonNotifyComponentSnapshot.toNotification(
    componentId: ComponentId,
    meta: SnapshotMeta
) = AppendSnapshot(
        componentId,
        meta,
        message.asJsonObject.toValue(),
        oldState.asJsonObject.toValue(),
        newState.asJsonObject.toValue()
)

private fun GsonNotifyComponentAttached.toNotification(
    id: ComponentId
) = ComponentAttached(id, state.asJsonObject.toValue())
