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
import com.oliynick.max.elm.core.loop.ComponentInternal
import com.oliynick.max.elm.core.loop.loop
import com.oliynick.max.elm.core.loop.newComponent
import com.oliynick.max.elm.core.loop.safe
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import protocol.*
import java.net.URL
import java.util.*

private val httpClient by lazy { HttpClient { install(WebSockets) } }
private val localhost by lazy { URL("http://localhost:8080") }

@DslMarker
private annotation class DslBuilder

interface JsonConverter {
    fun toJson(any: Any): String
    fun <T> fromJson(json: String, cl: Class<T>): T
}

//todo add dsl
data class DebugDependencies<M, C, S>(
    inline val componentDependencies: Dependencies<M, C, S>,
    inline val serverSettings: ServerSettings
)

//todo add dsl
data class ServerSettings(
    inline val id: ComponentId,
    inline val jsonConverter: JsonConverter,
    inline val converters: Converters = converters(),
    inline val url: URL = localhost
)

@DslBuilder
class ServerSettingsBuilder internal constructor(
    var id: ComponentId,
    var jsonConverter: JsonConverter,
    var converters: Converters = converters(),
    var url: URL = localhost
) {

    fun converters(config: Converters.() -> Unit) {
        converters.apply(config)
    }
}

@DslBuilder
class DebugDependenciesBuilder<M, C, S> internal constructor(
    var dependenciesBuilder: DependenciesBuilder<M, C, S>,
    var serverSettingsBuilder: ServerSettingsBuilder
) {

    fun dependencies(config: DependenciesBuilder<M, C, S>.() -> Unit) {
        dependenciesBuilder.apply(config)
    }

    fun serverSettings(config: ServerSettingsBuilder.() -> Unit) {
        serverSettingsBuilder.apply(config)
    }

}

fun <M, C, S> debugDependencies(
    id: ComponentId,
    jsonConverter: JsonConverter,
    dependencies: Dependencies<M, C, S>,
    config: DebugDependenciesBuilder<M, C, S>.() -> Unit = {}
) = DebugDependenciesBuilder(
    DependenciesBuilder(dependencies),
    ServerSettingsBuilder(id, jsonConverter)
).apply(config).toDebugDependencies()

fun <M, C, S> CoroutineScope.debugComponent(
    id: ComponentId,
    jsonConverter: JsonConverter,
    dependencies: Dependencies<M, C, S>,
    config: DebugDependenciesBuilder<M, C, S>.() -> Unit = {}
) = component(
    debugDependencies(
        id,
        jsonConverter,
        dependencies,
        config
    )
)

fun <M, C, S> CoroutineScope.component(debugDependencies: DebugDependencies<M, C, S>): Component<M, S> {

    val (messages, states) = webSocketComponent(debugDependencies)

    return newComponent(states, messages)
}

private fun <M, C, S> CoroutineScope.webSocketComponent(dependencies: DebugDependencies<M, C, S>): ComponentInternal<M, S> {

    val messages = Channel<M>()
    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    launch {

        httpClient.ws(
            HttpMethod.Get,
            dependencies.serverSettings.url.host,
            dependencies.serverSettings.url.port
        ) {

            val snapshots = Channel<NotifyComponentSnapshot<M, S>>()

            with(dependencies.withSpyingInterceptor(snapshots)) {
                // says 'hello' to a server; the message will be suspended until
                // the very first state gets computed
                launch { notifyAttached(statesChannel.asFlow().first(), serverSettings) }
                // observes state changes and notifies server about them
                launch { notifySnapshots(snapshots.consumeAsFlow(), serverSettings) }
                // observes incoming messages from server and applies them
                observeMessages(this@with, messages, statesChannel)
            }
        }
    }

    return messages to statesChannel.asFlow()
}

private suspend fun <M, C, S> ClientWebSocketSession.observeMessages(
    dependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(dependencies) {

        var computationJob = launch { loop(componentDependencies, messages, states) }

        suspend fun applyMessage(message: ClientMessage) {
            @Suppress("UNCHECKED_CAST")
            when (message) {
                is ApplyMessage -> messages.send(message.messageValue.fromValue(serverSettings.converters) as M)
                is ApplyState -> {
                    // cancels previous computation job and starts a new one
                    computationJob.cancel()
                    computationJob =
                        launch { loop(message.stateValue as Value<S>, this@with, messages, states) }
                }
            }.safe
        }

        incomingPackets(serverSettings.id, serverSettings.jsonConverter)
            .collect { packet ->

                println("In message $packet")

                applyMessage(packet.message)
                notifyApplied(serverSettings.jsonConverter, packet.id, serverSettings.id)
            }
    }
}

private fun ClientWebSocketSession.incomingPackets(id: ComponentId, jsonConverter: JsonConverter) =
    incoming.consumeAsFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> jsonConverter.fromJson(json, NotifyClient::class.java) }
        .filter { packet -> packet.component == id }

private suspend fun <M, C, S> loop(
    stateValue: Value<S>,
    inputDependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(inputDependencies) {
        loop(
            componentDependencies.withNewInitializer(stateValue.fromValue(serverSettings.converters)!!),
            messages,
            states
        )
    }
}

private suspend fun <S> WebSocketSession.notifyAttached(first: S, serverSettings: ServerSettings) {
    val message = notifyMessage(
        NotifyComponentAttached(first.toValue(serverSettings.converters)),
        serverSettings.id
    )

    send(serverSettings.jsonConverter.toJson(message))
}

private suspend fun <M, S> WebSocketSession.notifySnapshots(
    snapshots: Flow<NotifyComponentSnapshot<M, S>>,
    serverSettings: ServerSettings
) {
    snapshots.collect { snapshot ->
        send(
            serverSettings.jsonConverter,
            serverSettings.id,
            snapshot
        )
    }
}

private fun notifyMessage(
    message: ServerMessage,
    componentId: ComponentId,
    packetId: UUID = UUID.randomUUID()
) =
    NotifyServer(packetId, componentId, message)

private suspend fun WebSocketSession.send(
    jsonConverter: JsonConverter,
    componentId: ComponentId,
    message: ServerMessage
) {
    send(jsonConverter.toJson(NotifyServer(UUID.randomUUID(), componentId, message)))
}

private suspend fun WebSocketSession.notifyApplied(
    jsonConverter: JsonConverter,
    packetId: UUID,
    componentId: ComponentId
) {
    send(jsonConverter.toJson(NotifyServer(packetId, componentId, ActionApplied(packetId))))
}

private fun <M, C, S> spyingInterceptor(
    sink: SendChannel<NotifyComponentSnapshot<M, S>>,
    converters: Converters
): Interceptor<M, S, C> = { message, prevState, newState, _ ->
    sink.send(
        NotifyComponentSnapshot(
            message.toValue(converters),
            prevState.toValue(converters),
            newState.toValue(converters)
        )
    )
}

private fun <M, C, S> DebugDependencies<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot<M, S>>
) = copy(
    componentDependencies = componentDependencies.withSpyingInterceptor(
        snapshots,
        serverSettings.converters
    )
)

private fun <M, C, S> Dependencies<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot<M, S>>,
    converters: Converters
) = copy(interceptor = spyingInterceptor<M, C, S>(snapshots, converters).with(interceptor))

private fun <M, C, S> Dependencies<M, C, S>.withNewInitializer(s: S) =
    copy(initializer = { s to emptySet() })

private fun <M, C, S> DebugDependenciesBuilder<M, C, S>.toDebugDependencies() =
    DebugDependencies(
        dependenciesBuilder.toDependencies(),
        serverSettingsBuilder.toServerSettings()
    )

private fun ServerSettingsBuilder.toServerSettings() =
    ServerSettings(id, jsonConverter, converters, url)
