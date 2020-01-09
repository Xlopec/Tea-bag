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

@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.core.loop.ComponentInternal
import com.oliynick.max.elm.core.loop.loop
import com.oliynick.max.elm.core.loop.newComponent
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import protocol.*
import java.net.URL
import java.util.*

//TODO refactor internal api!

@PublishedApi
internal val httpClient by lazy { HttpClient { install(WebSockets) } }
@PublishedApi
internal val localhost by lazy(::URL)

@DslMarker
private annotation class DslBuilder

//todo add dsl
data class DebugDependencies<M, C, S>(
    inline val componentEnv: Env<M, C, S>,
    inline val serverSettings: ServerSettings
)

//todo add dsl
data class ServerSettings(
    inline val id: ComponentId,
    inline val serializer: JsonConverter,
    inline val url: URL
)

@DslBuilder
class ServerSettingsBuilder internal constructor(
    var id: ComponentId,
    var url: URL,
    var jsonSerializer: JsonConverter
) {

    fun installSerializer(
        serializer: JsonConverter
    ) {
        jsonSerializer = serializer
    }

}

fun URL(
    protocol: String = "http",
    host: String = "localhost",
    port: UInt = 8080U
) =
    URL(protocol, host, port.toInt(), "")

@DslBuilder
class DebugEnvBuilder<M, C, S> internal constructor(
    var dependenciesBuilder: EnvBuilder<M, C, S>,
    var serverSettingsBuilder: ServerSettingsBuilder
) {

    fun dependencies(config: EnvBuilder<M, C, S>.() -> Unit) {
        dependenciesBuilder.apply(config)
    }

    fun serverSettings(config: ServerSettingsBuilder.() -> Unit) {
        serverSettingsBuilder.apply(config)
    }

}

fun <M, C, S> Dependencies(
    id: ComponentId,
    env: Env<M, C, S>,
    url: URL = localhost,
    serializer: JsonConverter = gsonSerializer(),
    config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = DebugEnvBuilder(
    EnvBuilder(env),
    ServerSettingsBuilder(id, url, serializer)
).apply(config).toDebugDependencies()

inline fun <reified M, reified C, reified S> CoroutineScope.Component(
    id: ComponentId,
    env: Env<M, C, S>,
    url: URL = localhost,
    serializer: JsonConverter = gsonSerializer(),
    noinline config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = Component(
    Dependencies(
        id,
        env,
        url,
        serializer,
        config
    )
)

inline fun <reified M, reified C, reified S> CoroutineScope.Component(
    debugDependencies: DebugDependencies<M, C, S>
): Component<M, S> {

    val (messages, states) = webSocketComponent(debugDependencies)

    return newComponent(states, messages)
}

@PublishedApi
internal inline fun <reified M, reified C, reified S> CoroutineScope.webSocketComponent(
    dependencies: DebugDependencies<M, C, S>
): ComponentInternal<M, S> {

    val messages = Channel<M>()
    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    launch {
        try {
            connect(messages, statesChannel, dependencies)
        } catch (th: Exception) {
            throw RuntimeException("Failed to connect " +
                                       "to the host: ${dependencies.serverSettings.url}", th)
        }
    }

    return messages to statesChannel.asFlow()
}

@PublishedApi
internal suspend inline fun <reified M, reified C, reified S> connect(
    messages: Channel<M>,
    statesChannel: BroadcastChannel<S>,
    dependencies: DebugDependencies<M, C, S>
) =
    httpClient.ws(
        HttpMethod.Get,
        dependencies.serverSettings.url.host,
        dependencies.serverSettings.url.port
    ) {

        val snapshots = Channel<NotifyComponentSnapshot>()

        with(dependencies.withSpyingInterceptor(snapshots, dependencies.serverSettings.serializer)) {
            launch {
                // says 'hello' to a server; the message will be suspended until
                // the very first state gets computed
                notifyAttached(statesChannel.asFlow().first(), serverSettings)
                // observes state changes and notifies server about them
                notifySnapshots(snapshots.consumeAsFlow(), serverSettings)
            }
            // observes incoming messages from server and applies them
            observeMessages<M, C, S>(this@with, messages, statesChannel)
        }
    }

@PublishedApi
internal suspend inline fun <reified M, C, reified S> ClientWebSocketSession.observeMessages(
    dependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(dependencies) {

        var computationJob = launch { componentEnv.loop(messages, states) }

        incomingPackets(serverSettings.id, serverSettings.serializer)
            .collect { packet ->
                // todo consider replacing with some kind of stream and fold function
                val newJob = applyMessage(packet.message, dependencies, messages, states)

                if (newJob != null) {
                    computationJob.cancel()
                    computationJob = newJob
                }

                notifyApplied(packet.id, serverSettings.id, serverSettings.serializer)
            }
    }
}

@PublishedApi
internal suspend inline fun <reified M, C, reified S> ClientWebSocketSession.applyMessage(
    message: ClientMessage,
    dependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
): Job? = with(dependencies) {
    @Suppress("UNCHECKED_CAST")
    when (message) {
        // todo split into functions per message type
        is ApplyMessage -> {
            messages.send(dependencies.serverSettings.serializer.fromJsonTree(message.message, M::class.java))
            null
        }
        is ApplyState -> {
            // cancels previous computation job and starts a new one
            launch {
                loop<M, C, S>(
                    dependencies.serverSettings.serializer.fromJsonTree(message.state, S::class.java),
                    this@with,
                    messages,
                    states
                )
            }
        }
    }
}

@PublishedApi
internal fun ClientWebSocketSession.incomingPackets(
    id: ComponentId,
    serializer: JsonConverter
) =
    incoming.consumeAsFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> serializer.fromJson(json, NotifyClient::class.java) }
        .filter { packet -> packet.component == id }

@PublishedApi
internal suspend inline fun <M, C, reified S> loop(
    stateValue: S,
    inputDependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(inputDependencies) {
        componentEnv.withNewInitializer(stateValue)
            .loop(
                messages,
                states
            )
    }
}

@PublishedApi
internal suspend inline fun <reified S> WebSocketSession.notifyAttached(
    first: S,
    serverSettings: ServerSettings
) {
    val message = notifyMessage(
        NotifyComponentAttached(serverSettings.serializer.toJsonTree(first as Any)),
        serverSettings.id
    )

    send(serverSettings.serializer.toJson(message))
}

@PublishedApi
internal suspend fun WebSocketSession.notifySnapshots(
    snapshots: Flow<NotifyComponentSnapshot>,
    serverSettings: ServerSettings
) {
    snapshots.collect { snapshot ->
        send(
            serverSettings.id,
            snapshot,
            serverSettings.serializer
        )
    }
}

@PublishedApi
internal fun notifyMessage(
    message: ServerMessage,
    componentId: ComponentId,
    packetId: UUID = UUID.randomUUID()
) =
    NotifyServer(packetId, componentId, message)

@PublishedApi
internal suspend fun WebSocketSession.send(
    componentId: ComponentId,
    message: ServerMessage,
    serializer: JsonConverter
) {
    send(serializer.toJson(NotifyServer(UUID.randomUUID(), componentId, message)))
}

@PublishedApi
internal suspend fun WebSocketSession.notifyApplied(
    packetId: UUID,
    componentId: ComponentId,
    serializer: JsonConverter
) {
    send(serializer.toJson(NotifyServer(packetId, componentId, ActionApplied(packetId))))
}

@PublishedApi
internal inline fun <reified M, reified C, reified S> spyingInterceptor(
    sink: SendChannel<NotifyComponentSnapshot>,
    serializer: JsonConverter
): Interceptor<M, S, C> = { message, prevState, newState, _ ->
    sink.send(
        NotifyComponentSnapshot(
            serializer.toJsonTree(message as Any),
            serializer.toJsonTree(prevState as Any),
            serializer.toJsonTree(newState as Any)
        )
    )
}

@PublishedApi
internal inline fun <reified M, reified C, reified S> DebugDependencies<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot>,
    serializer: JsonConverter
) = copy(
    componentEnv = componentEnv.withSpyingInterceptor(
        snapshots,
        serializer
    )
)

@PublishedApi
internal inline fun <reified M, reified C, reified S> Env<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot>,
    serializer: JsonConverter
) = copy(interceptor = spyingInterceptor<M, C, S>(snapshots, serializer).with(interceptor))

@PublishedApi
internal fun <M, C, S> Env<M, C, S>.withNewInitializer(s: S) =
    copy(initializer = { s to emptySet() })

@PublishedApi
internal fun <M, C, S> DebugEnvBuilder<M, C, S>.toDebugDependencies() =
    DebugDependencies(
        dependenciesBuilder.toEnv(),
        serverSettingsBuilder.toServerSettings()
    )

@PublishedApi
internal fun ServerSettingsBuilder.toServerSettings() =
    ServerSettings(id, jsonSerializer, url)
