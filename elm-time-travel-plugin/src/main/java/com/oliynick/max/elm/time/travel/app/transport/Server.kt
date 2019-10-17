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
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import org.slf4j.event.Level
import java.lang.IllegalStateException
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

data class RemoteCallArgs(val callId: UUID, val component: ComponentId, val message: Message)

@Suppress("MemberVisibilityCanBePrivate")
class Server private constructor(
    val settings: Settings,
    private val events: Channel<PluginMessage>,//fixme broadcast
    private val completions: BroadcastChannel<UUID>,
    private val calls: BroadcastChannel<RemoteCallArgs>
) : ApplicationEngine by server(settings, events, completions, calls) {

    companion object {

        private const val timeout = 3000L

        fun newInstance(settings: Settings, events: Channel<PluginMessage>) = Server(settings, events, BroadcastChannel(1), BroadcastChannel(1))
    }

    suspend operator fun invoke(component: ComponentId, message: Message) {
        try {
            withTimeout(timeout) {
                //todo un-hardcode
                val callId = UUID.randomUUID()
                val completionJob = launch { completions.asFlow().first { id -> id == callId } }

                calls.send(RemoteCallArgs(callId, component, message))
                completionJob.join()
            }
        } catch (th: TimeoutCancellationException) {
            throw TimeoutException("Timed out waiting for $timeout ms to perform operation")
        } catch (th: ClosedSendChannelException) {
            throw ClientHasGoneException("Send channel was closed, the client, probably, has disconnected", th)
        } catch (th: CancellationException) {
            throw ClientHasGoneException("Send channel was closed, the client, probably, has disconnected", th)
        }
    }

}

class ClientHasGoneException(message: String, cause: Throwable) : Throwable(message, cause)

private fun server(
    settings: Settings,
    events: Channel<PluginMessage>,
    completions: BroadcastChannel<UUID>,
    calls: BroadcastChannel<RemoteCallArgs>
): NettyApplicationEngine {

    return embeddedServer(Netty, host = settings.serverSettings.host, port = settings.serverSettings.port.toInt()) {

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

        installErrorInterceptors()

        routing {

            webSocket("/") {

                launch {
                    calls.asFlow().collect { (callId, componentId, message) ->
                        send(SendPacket.pack(callId, componentId, message))
                    }
                }

                webSocketSessionDispatcher(settings, this).use { dispatcher ->

                    incoming.consumeAsFlow()
                        .filterIsInstance<Frame.Binary>()
                        .collect { frame ->

                            withContext(dispatcher) {
                                val packetDeferred = async { ReceivePacket.unpack(frame.readBytes()) }

                                try {
                                    val packet = packetDeferred.await()

                                    when (val message = (packet.message as? ServerMessage)) {
                                        is NotifyComponentSnapshot -> events.send(AppendSnapshot(packet.component, message.message, message.oldState, message.newState))
                                        is NotifyComponentAttached -> events.send(ComponentAttached(packet.component, message.state))
                                        is ActionApplied -> completions.send(message.id)
                                    }

                                } catch (e: Throwable) {
                                    events.send(NotifyOperationException(e))
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