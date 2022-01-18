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

import com.oliynick.max.tea.core.debug.app.component.cms.message.AppendSnapshot
import com.oliynick.max.tea.core.debug.app.component.cms.message.ComponentAttached
import com.oliynick.max.tea.core.debug.app.component.cms.message.Message
import com.oliynick.max.tea.core.debug.app.component.cms.message.NotifyOperationException
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.SnapshotMeta
import com.oliynick.max.tea.core.debug.app.transport.serialization.GSON
import com.oliynick.max.tea.core.debug.app.transport.serialization.toCollectionWrapper
import com.oliynick.max.tea.core.debug.app.transport.serialization.toValue
import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.gson.GsonNotifyComponentAttached
import com.oliynick.max.tea.core.debug.gson.GsonNotifyComponentSnapshot
import com.oliynick.max.tea.core.debug.gson.GsonNotifyServer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.NotifyClient
import com.oliynick.max.tea.core.debug.protocol.NotifyServer
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.event.Level
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.*
import java.util.UUID.randomUUID

class ServerImpl private constructor(
    private val address: ServerAddress,
    private val events: MutableSharedFlow<Message>,
    private val calls: BroadcastChannel<RemoteCallArgs>
) : ApplicationEngine by Server(address, events, calls), Server {

    companion object {

        fun newInstance(
            address: ServerAddress,
            events: MutableSharedFlow<Message>
        ): ServerImpl = ServerImpl(address, events, BroadcastChannel(1))

    }

    override suspend operator fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    ) = withContext(Dispatchers.IO) {
        calls.send(RemoteCallArgs(randomUUID(), component, message))
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
    events: MutableSharedFlow<Message>,
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

        install(Routing)
        install(ConditionalHeaders)

        install(io.ktor.server.websocket.WebSockets) {
            pingPeriod = Duration.ofSeconds(10)
            timeout = Duration.ofSeconds(5)
        }

        configureWebSocketRouting(calls.asFlow(), events)
    }

private fun Application.configureWebSocketRouting(
    calls: Flow<RemoteCallArgs>,
    events: MutableSharedFlow<Message>
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
    events: MutableSharedFlow<Message>,
    incoming: Flow<Frame.Text>
) {
    incoming.collect { frame -> processPacket(frame, events) }
}

private suspend fun processPacket(
    frame: Frame.Text,
    events: MutableSharedFlow<Message>
) =
    withContext(Dispatchers.IO) {
        try {
            val json = frame.readText()
            val packet = GSON.fromJson<GsonNotifyServer>(json, NotifyServer::class.java)

            when (val message = packet.payload) {
                is GsonNotifyComponentSnapshot -> events.emit(
                    message.toNotification(packet.componentId, SnapshotMeta())
                )
                is GsonNotifyComponentAttached -> events.emit(
                    message.toNotification(packet.componentId, SnapshotMeta())
                )
            }

        } catch (e: Throwable) {
            events.emit(NotifyOperationException(e))
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
    newState.asJsonObject.toValue(),
    commands.toCollectionWrapper(),
)

private fun GsonNotifyComponentAttached.toNotification(
    id: ComponentId,
    meta: SnapshotMeta
) = ComponentAttached(id, meta, state.asJsonObject.toValue(), commands.toCollectionWrapper())

private fun SnapshotMeta() = SnapshotMeta(SnapshotId(randomUUID()), now())
