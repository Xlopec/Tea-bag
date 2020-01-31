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

package com.oliynick.max.elm.time.travel.app.transport

import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.app.transport.serialization.GSON
import com.oliynick.max.elm.time.travel.app.transport.serialization.toValue
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DataConversion
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
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
import protocol.*
import java.time.Duration
import java.util.*

data class RemoteCallArgs(
    val callId: UUID,
    val component: ComponentId,
    val message: ClientMessage
)

@Suppress("MemberVisibilityCanBePrivate")
class Server private constructor(
    val settings: Settings,
    private val events: BroadcastChannel<PluginMessage>,
    private val calls: BroadcastChannel<RemoteCallArgs>
) : ApplicationEngine by server(settings, events, calls) {

    companion object {

        private const val timeout = 3000L

        fun newInstance(
            settings: Settings,
            events: BroadcastChannel<PluginMessage>
        ): Server = Server(settings, events, BroadcastChannel(1))

    }

    suspend operator fun invoke(
        component: ComponentId,
        message: ClientMessage
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

fun main() {
    runBlocking {
        embeddedServer(Netty, host = "localhost", port = 8080) {

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

            routing {

                webSocket("/") {

                    val pluginMessages = BroadcastChannel<PluginMessage>(1)
                    val completions = BroadcastChannel<UUID>(1)

                    launch {
                        for (msg in pluginMessages.openSubscription()) {
                            println("Plugin message $msg")
                        }
                    }

                    launch {
                        for (c in completions.openSubscription()) {
                            println("Plugin message $c")
                        }
                    }

                    installPacketReceiver(
                        pluginMessages,
                        incoming.consumeAsFlow().filterIsInstance()
                    )
                }
            }
        }.start(true)
    }
}

private fun server(
    settings: Settings,
    events: BroadcastChannel<PluginMessage>,
    calls: BroadcastChannel<RemoteCallArgs>
): NettyApplicationEngine {

    return embeddedServer(
        Netty,
        host = settings.serverSettings.host,
        port = settings.serverSettings.port.toInt()
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

        println("Send json $json")

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
    coroutineScope {

        val json = frame.readText()

        println("Got json $json")

        val packet = GSON.fromJson(json, NotifyServer::class.java)

        try {

            when (val message = packet.payload) {// todo consider removing `?`
                is NotifyComponentSnapshot -> events.send(
                    AppendSnapshot(
                        packet.componentId,
                        message.message.asJsonObject.toValue(),
                        message.oldState.asJsonObject.toValue(),
                        message.newState.asJsonObject.toValue()
                    )
                )
                is NotifyComponentAttached -> events.send(
                    ComponentAttached(
                        packet.componentId,
                        message.state.asJsonObject.toValue()
                    )
                )
                is ActionApplied -> error("ActionApplied instances shouldn't be sent anymore")
            }

        } catch (e: Throwable) {
            events.send(NotifyOperationException(e))
        }
    }
}

