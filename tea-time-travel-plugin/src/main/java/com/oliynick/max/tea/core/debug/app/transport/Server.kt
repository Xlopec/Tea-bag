/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.tea.core.debug.app.transport

import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.transport.serialization.GSON
import com.oliynick.max.tea.core.debug.app.transport.serialization.toValue
import com.oliynick.max.tea.core.debug.gson.*
import com.oliynick.max.tea.core.debug.protocol.*
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import org.slf4j.event.Level
import java.time.Duration
import java.util.*

data class RemoteCallArgs(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)

@Suppress("MemberVisibilityCanBePrivate")
class Server private constructor(
    val address: ServerAddress,
    private val events: BroadcastChannel<PluginMessage>,
    private val calls: BroadcastChannel<RemoteCallArgs>
) : ApplicationEngine by server(address, events, calls) {

    companion object {

        private const val timeout = 3000L

        fun newInstance(
            address: ServerAddress,
            events: BroadcastChannel<PluginMessage>
        ): Server = Server(address, events, BroadcastChannel(1))

    }

    suspend operator fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    ) {
        try {
            withTimeout(timeout) {
                //todo un-hardcode
                val callId = UUID.randomUUID()
                //val completionJob = launch { completions.asFlow().first { id -> id == callId } }

                calls.send(RemoteCallArgs(callId, component, message))
                // completionJob.join()
            }
        } catch (th: TimeoutCancellationException) {
            throw NetworkException("Timed out waiting for $timeout ms to perform operation",
                th)
        } catch (th: Throwable) {
            throw th.toPluginException()
        }
    }

}

private fun server(
    address: ServerAddress,
    events: BroadcastChannel<PluginMessage>,
    calls: BroadcastChannel<RemoteCallArgs>
): NettyApplicationEngine {

    return embeddedServer(
        Netty,
        host = address.host.value,
        port = address.port.value.toInt()
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
) {
    calls.collect { (callId, componentId, message) ->

        val json = GSON.toJson(NotifyClient(callId, componentId, message))

        outgoing.send(Frame.Text(json))
    }
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
) {
    val json = frame.readText()
    val packet = GSON.fromJson<GsonNotifyServer>(json, NotifyServer::class.java)

    try {

        when (val message = packet.payload) {
            is GsonNotifyComponentSnapshot -> events.send(
                AppendSnapshot(
                    packet.componentId,
                    message.message.asJsonObject.toValue(),
                    message.oldState.asJsonObject.toValue(),
                    message.newState.asJsonObject.toValue()
                )
            )
            is GsonNotifyComponentAttached -> events.send(
                ComponentAttached(
                    packet.componentId,
                    message.state.asJsonObject.toValue()
                )
            )
        }

    } catch (e: Throwable) {
        events.send(NotifyOperationException(e))
    }
}

