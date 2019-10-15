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

import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.misc.FileSystemClassLoader
import com.oliynick.max.elm.time.travel.app.transport.exception.findCause
import com.oliynick.max.elm.time.travel.app.transport.exception.installErrorInterceptors
import com.oliynick.max.elm.time.travel.protocol.*
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DataConversion
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.event.Level
import java.time.Duration
import java.util.concurrent.Executors

fun server(
    settings: Settings,
    events: Channel<PluginMessage>,
    outgoing: Channel<Pair<ComponentId, Message>>
): NettyApplicationEngine {

    return embeddedServer(Netty, host = settings.serverSettings.host, port = settings.serverSettings.port.toInt()) {

        install(CallLogging) {
            level = Level.INFO
            filter { call -> call.request.path().startsWith("/") }
        }

        install(ConditionalHeaders)
        install(DataConversion)

        install(DefaultHeaders) { header("X-Engine", "Ktor") }

        install(io.ktor.websocket.WebSockets) {
            pingPeriod = Duration.ofSeconds(10)
            timeout = Duration.ofSeconds(5)
        }

        installErrorInterceptors()

        routing {

            webSocket("/") {

                launch {
                    for ((id, action) in outgoing) {
                        send(SendPacket.pack(id, action))
                    }
                }

                webSocketSessionDispatcher(settings, this).use { dispatcher ->

                    for (frame in incoming.of(Frame.Binary::class)) {

                        require(frame.fin) { "Chunks aren't supported" }

                        withContext(dispatcher) {

                            val result = async { ReceivePacket.unpack(frame.readBytes()).toMessage() }

                            try {
                                events.send(result.await())
                            } catch (e: Throwable) {
                                events.notifyException(e)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun webSocketSessionDispatcher(settings: Settings, session: WebSocketServerSession): ExecutorCoroutineDispatcher {
    return Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "web socket thread $session")
            .also { it.contextClassLoader = FileSystemClassLoader(settings.classFiles) }
    }.asCoroutineDispatcher()
}

private suspend fun Channel<PluginMessage>.notifyException(e: Throwable) {
    val message = e.findCause { cause -> cause is ClassNotFoundException }
        ?.let { it as ClassNotFoundException }
        ?.let(::NotifyMissingDependency) ?: NotifyOperationException(e)

    send(message)
}

private fun ReceivePacket.toMessage(): PluginMessage {
    return when (val action = message) {
        is NotifyComponentSnapshot -> AppendSnapshot(component, action.message, action.oldState, action.newState)
        is ApplyMessage -> TODO("shouldn't get here")
        is ApplyState -> TODO("shouldn't get here")
        is NotifyComponentAttached -> ComponentAttached(component, action.state)
    }
}