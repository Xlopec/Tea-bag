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

package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.core.loop.*
import com.oliynick.max.elm.time.travel.protocol.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val httpClient by lazy { HttpClient { install(WebSockets) } }
private val localhost = Host("localhost")

data class Host(val value: String) {
    init {
        require(value.isNotEmpty() && value.isNotEmpty()) { "Hostname shouldn't be empty" }
    }
}

data class Settings(val id: ComponentId, val host: Host = localhost, val port: UInt = 8080U)

fun <M : Any, C : Any, S : Any> CoroutineScope.component(settings: Settings,
                                                         initialState: S,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor,
                                                         vararg initialCommands: C): Component<M, S> {

    @Suppress("RedundantSuspendModifier")
    suspend fun loader() = initialState to setOf(*initialCommands)

    return component(settings, ::loader, resolver, update, interceptor)
}

fun <M : Any, C : Any, S : Any> CoroutineScope.component(settings: Settings,
                                                         initializer: Initializer<S, C>,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor): Component<M, S> {

    val (messages, states) = webSocketComponent(settings, initializer, resolver, update, interceptor)

    return newComponent(states, messages)
}

private fun <M : Any, C : Any, S : Any> CoroutineScope.webSocketComponent(settings: Settings,
                                                                          initializer: Initializer<S, C>,
                                                                          resolver: Resolver<C, M>,
                                                                          update: Update<M, S, C>,
                                                                          interceptor: Interceptor<M, S, C>): ComponentInternal<M, S> {

    val snapshots = Channel<NotifyComponentSnapshot>()
    val dependencies = Dependencies(initializer, resolver, update, spyingInterceptor<M, C, S>(snapshots).with(interceptor))
    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)
    val messages = Channel<M>()

    launch {
        httpClient.ws(HttpMethod.Get, settings.host.value, settings.port.toInt()) {
            // says 'hello' to a server; 'send' call will be suspended until the very first state gets computed
            launch { send(settings.id, NotifyComponentAttached(statesChannel.asFlow().first())) }

            var computationJob = launch { loop(initializer, dependencies, messages, statesChannel) }

            suspend fun applyMessage(message: ClientMessage) {
                @Suppress("UNCHECKED_CAST")
                when (message) {
                    is ApplyMessage -> messages.send(message.message as M)
                    is ApplyState -> {
                        // cancels previous computation job and starts a new one
                        computationJob.cancel()
                        computationJob = launch { loop({ message.state as S to emptySet() }, dependencies, messages, statesChannel) }
                    }
                }.safe
            }
            // observes changes and notifies the server
            launch { snapshots.consumeAsFlow().collect { snapshot -> send(settings.id, snapshot) } }
            // parses and applies incoming messages
            incoming.consumeAsFlow()
                .filterIsInstance<Frame.Binary>()
                .map { frame -> ReceivePacket.unpack(frame.readBytes()) }
                .filterIsInstance<ClientMessage>()
                .collect { message -> applyMessage(message) }
        }
    }

    return messages to statesChannel.asFlow()
}

private suspend fun WebSocketSession.send(id: ComponentId, message: ServerMessage) = send(SendPacket.pack(id, message))

private fun <M : Any, C : Any, S : Any> spyingInterceptor(sink: SendChannel<NotifyComponentSnapshot>): Interceptor<M, S, C> {
    return { message, prevState, newState, _ -> sink.send(NotifyComponentSnapshot(message, prevState, newState)) }
}