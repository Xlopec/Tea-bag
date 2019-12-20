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

internal interface JsonConverter {
    fun toJson(any: Any): String
    fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T
}

//todo add dsl
data class DebugDependencies<M, C, S>(
    inline val componentEnv: Env<M, C, S>,
    inline val serverSettings: ServerSettings
)

//todo add dsl
data class ServerSettings(
    inline val id: ComponentId,
    inline val serializer: JsonSerializer,
    inline val url: URL
)

interface JsonSerializer {

    fun <T> toJson(
        any: T,
        type: Class<out T>
    ): Json

    fun <T> fromJson(
        json: Json,
        type: Class<out T>
    ): T?

}

@DslBuilder
class ServerSettingsBuilder internal constructor(
    var id: ComponentId,
    var url: URL,
    var jsonSerializer: JsonSerializer
) {

    fun installSerializer(
        serializer: JsonSerializer
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
    serializer: JsonSerializer = gsonSerializer(),
    config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = DebugEnvBuilder(
    EnvBuilder(env),
    ServerSettingsBuilder(id, url, serializer)
).apply(config).toDebugDependencies()

inline fun <reified M, C, reified S> CoroutineScope.Component(
    id: ComponentId,
    env: Env<M, C, S>,
    url: URL = localhost,
    serializer: JsonSerializer = gsonSerializer(),
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

inline fun <reified M, C, reified S> CoroutineScope.Component(debugDependencies: DebugDependencies<M, C, S>): Component<M, S> {

    val (messages, states) = webSocketComponent(debugDependencies)

    return newComponent(states, messages)
}

@PublishedApi
internal inline fun <reified M, C, reified S> CoroutineScope.webSocketComponent(dependencies: DebugDependencies<M, C, S>): ComponentInternal<M, S> {

    val messages = Channel<M>()
    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    launch {
        // todo add IO exceptions handling
        httpClient.ws(
            HttpMethod.Get,
            dependencies.serverSettings.url.host,
            dependencies.serverSettings.url.port
        ) {

            val snapshots = Channel<NotifyComponentSnapshot>()

            with(dependencies.withSpyingInterceptor(snapshots)) {
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
    }

    return messages to statesChannel.asFlow()
}

@PublishedApi
internal suspend inline fun <reified M, C, reified S> ClientWebSocketSession.observeMessages(
    dependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(dependencies) {

        var computationJob = launch { componentEnv.loop(messages, states) }

        incomingPackets(serverSettings.id)
            .collect { packet ->
                // todo consider replacing with some kind of stream and fold function
                val newJob = applyMessage(packet.message, dependencies, messages, states)

                if (newJob != null) {
                    computationJob.cancel()
                    computationJob = newJob
                }

                notifyApplied(packet.id, serverSettings.id)
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
            messages.send(message.message as M)
            null
        }
        is ApplyState -> {
            // cancels previous computation job and starts a new one
            launch { loop<M, C, S>(message.state as S, this@with, messages, states) }
        }
    }
}

@PublishedApi
internal fun ClientWebSocketSession.incomingPackets(id: ComponentId) =
    incoming.consumeAsFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> GsonConverter.fromJson(json, NotifyClient::class.java) }
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
internal suspend fun <S> WebSocketSession.notifyAttached(
    first: S,
    serverSettings: ServerSettings
) {
    val message = notifyMessage(
        NotifyComponentAttached(first as Any),
        serverSettings.id
    )

    send(GsonConverter.toJson(message))
}

@PublishedApi
internal suspend fun WebSocketSession.notifySnapshots(
    snapshots: Flow<NotifyComponentSnapshot>,
    serverSettings: ServerSettings
) {
    snapshots.collect { snapshot ->
        send(
            serverSettings.id,
            snapshot
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
    message: ServerMessage
) {
    send(GsonConverter.toJson(NotifyServer(UUID.randomUUID(), componentId, message)))
}

@PublishedApi
internal suspend fun WebSocketSession.notifyApplied(
    packetId: UUID,
    componentId: ComponentId
) {
    send(GsonConverter.toJson(NotifyServer(packetId, componentId, ActionApplied(packetId))))
}

@PublishedApi
internal fun <M, C, S> spyingInterceptor(
    sink: SendChannel<NotifyComponentSnapshot>
): Interceptor<M, S, C> = { message, prevState, newState, _ ->
    sink.send(
        NotifyComponentSnapshot(
            message as Any,
            prevState as Any,
            newState as Any
        )
    )
}

@PublishedApi
internal fun <M, C, S> DebugDependencies<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot>
) = copy(
    componentEnv = componentEnv.withSpyingInterceptor(
        snapshots
    )
)

@PublishedApi
internal fun <M, C, S> Env<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot>
) = copy(interceptor = spyingInterceptor<M, C, S>(snapshots).with(interceptor))

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
