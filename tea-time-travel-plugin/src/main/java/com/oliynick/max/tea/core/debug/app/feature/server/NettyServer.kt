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

package com.oliynick.max.tea.core.debug.app.feature.server

import com.google.gson.Gson
import com.oliynick.max.tea.core.debug.app.Message
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.SnapshotMeta
import com.oliynick.max.tea.core.debug.app.feature.notification.AppendSnapshot
import com.oliynick.max.tea.core.debug.app.feature.notification.ComponentAttached
import com.oliynick.max.tea.core.debug.app.feature.notification.OperationException
import com.oliynick.max.tea.core.debug.app.state.Server
import com.oliynick.max.tea.core.debug.gson.*
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.core.debug.protocol.NotifyClient
import io.github.xlopec.tea.core.debug.protocol.NotifyServer
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.event.Level
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.UUID.randomUUID

class NettyServer(
    override val address: ServerAddress,
    private val events: MutableSharedFlow<Message>,
    private val calls: MutableSharedFlow<RemoteCall> = MutableSharedFlow(),
    private val gson: Gson = Gson {}
) : ApplicationEngine by NettyAppEngine(address, events, calls, gson), Server {

    override suspend operator fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    ) = calls.emit(RemoteCall(randomUUID(), component, message))

    override suspend fun stop() =
        withContext(IO) {
            stop(1, 1)
        }

}

private fun NettyAppEngine(
    address: ServerAddress,
    events: MutableSharedFlow<Message>,
    calls: MutableSharedFlow<RemoteCall>,
    gson: Gson
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

        install(WebSockets.Plugin) {
            pingPeriod = Duration.ofSeconds(10)
            timeout = Duration.ofSeconds(5)
        }

        routing {

            webSocket("/") {

                launch {
                    calls.collect<RemoteCall> { call -> outgoing.processOutgoingCall(call, gson) }
                }

                incoming.consumeAsFlow().filterIsInstance<Frame.Text>()
                    .collect { frame -> processIncomingFrame(frame, events, gson) }
            }
        }
    }

private suspend fun SendChannel<Frame>.processOutgoingCall(
    remoteCall: RemoteCall,
    gson: Gson
) {
    val json = gson.toJson(NotifyClient(remoteCall.callId, remoteCall.component, remoteCall.message))

    send(Frame.Text(json))
}

private suspend fun processIncomingFrame(
    frame: Frame.Text,
    events: MutableSharedFlow<Message>,
    gson: Gson
) =
    withContext(IO) {
        try {
            val json = frame.readText()
            val packet = gson.fromJson<GsonNotifyServer>(json, NotifyServer::class.java)

            when (val message = packet.payload) {
                is GsonNotifyComponentSnapshot -> events.emit(
                    message.toNotification(packet.componentId, SnapshotMeta())
                )
                is GsonNotifyComponentAttached -> events.emit(
                    message.toNotification(packet.componentId, SnapshotMeta())
                )
            }

        } catch (e: Throwable) {
            events.emit(OperationException(e))
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
