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
private val localhost by lazy(::URL)

@DslMarker
private annotation class DslBuilder

internal interface JsonConverter {
    fun toJson(any: Any): String
    fun <T> fromJson(json: String, cl: Class<T>): T
}

//todo add dsl
data class DebugDependencies<M, C, S>(
    inline val componentEnv: Env<M, C, S>,
    inline val serverSettings: ServerSettings
)

//todo add dsl
data class ServerSettings(
    inline val id: ComponentId,
    inline val converters: Converters = converters(),
    inline val url: URL = localhost
)

@DslBuilder
class ServerSettingsBuilder<M, C, S> internal constructor(
    var id: ComponentId,
    var converters: Converters,
    var url: URL
) {

    @JvmName("unsafeConverters")
    fun converters(config: Converters.() -> Unit) {
        converters.apply(config)
    }

    @JvmName("messageConverters")
    fun converter(c: (M, Converters) -> Value<*>) {

    }

    @JvmName("commandConverters")
    fun converter(c: (C, Converters) -> Value<*>) {

    }

    @JvmName("stateConverters")
    fun converter(c: (S, Converters) -> Value<*>) {

    }

    fun <S> stateDeserializer(v: (Value<*>, Converters) -> S) {

    }

    fun <M> messageDeserializer(v: (Value<*>, Converters) -> M) {

    }

    fun <C> commandDeserializer(v: (Value<*>, Converters) -> C) {

    }

}

fun URL(protocol: String = "http", host: String = "localhost", port: UInt = 8080U) = URL(protocol, host, port.toInt(), "")

@DslBuilder
class DebugEnvBuilder<M, C, S> internal constructor(
    var dependenciesBuilder: EnvBuilder<M, C, S>,
    var serverSettingsBuilder: ServerSettingsBuilder<M, C, S>
) {

    fun dependencies(config: EnvBuilder<M, C, S>.() -> Unit) {
        dependenciesBuilder.apply(config)
    }

    fun serverSettings(config: ServerSettingsBuilder<M, C, S>.() -> Unit) {
        serverSettingsBuilder.apply(config)
    }

}

fun <M, C, S> Dependencies(
    id: ComponentId,
    env: Env<M, C, S>,
    url: URL = localhost,
    config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = DebugEnvBuilder(
    EnvBuilder(env),
    ServerSettingsBuilder(id, converters(), url)
).apply(config).toDebugDependencies()

fun <M, C, S> CoroutineScope.Component(
    id: ComponentId,
    env: Env<M, C, S>,
    url: URL = localhost,
    config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = Component(
    Dependencies(
        id,
        env,
        url,
        config
    )
)

fun <M, C, S> CoroutineScope.Component(debugDependencies: DebugDependencies<M, C, S>): Component<M, S> {

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
                launch {
                    // says 'hello' to a server; the message will be suspended until
                    // the very first state gets computed
                    notifyAttached(statesChannel.asFlow().first(), serverSettings)
                    // observes state changes and notifies server about them
                    notifySnapshots(snapshots.consumeAsFlow(), serverSettings)
                }
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

        var computationJob = launch { componentEnv.loop(messages, states) }

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

        incomingPackets(serverSettings.id)
            .collect { packet ->
                applyMessage(packet.message)
                notifyApplied(packet.id, serverSettings.id)
            }
    }
}

private fun ClientWebSocketSession.incomingPackets(id: ComponentId) =
    incoming.consumeAsFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> GsonConverter.fromJson(json, NotifyClient::class.java) }
        .filter { packet -> packet.component == id }

private suspend fun <M, C, S> loop(
    stateValue: Value<S>,
    inputDependencies: DebugDependencies<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(inputDependencies) {
        componentEnv.withNewInitializer(stateValue.fromValue(serverSettings.converters)!!)
            .loop(
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

    send(GsonConverter.toJson(message))
}

private suspend fun <M, S> WebSocketSession.notifySnapshots(
    snapshots: Flow<NotifyComponentSnapshot<M, S>>,
    serverSettings: ServerSettings
) {
    snapshots.collect { snapshot ->
        send(
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
    componentId: ComponentId,
    message: ServerMessage
) {
    send(GsonConverter.toJson(NotifyServer(UUID.randomUUID(), componentId, message)))
}

private suspend fun WebSocketSession.notifyApplied(
    packetId: UUID,
    componentId: ComponentId
) {
    send(GsonConverter.toJson(NotifyServer(packetId, componentId, ActionApplied(packetId))))
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
    componentEnv = componentEnv.withSpyingInterceptor(
        snapshots,
        serverSettings.converters
    )
)

private fun <M, C, S> Env<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot<M, S>>,
    converters: Converters
) = copy(interceptor = spyingInterceptor<M, C, S>(snapshots, converters).with(interceptor))

private fun <M, C, S> Env<M, C, S>.withNewInitializer(s: S) =
    copy(initializer = { s to emptySet() })

private fun <M, C, S> DebugEnvBuilder<M, C, S>.toDebugDependencies() =
    DebugDependencies(
        dependenciesBuilder.toEnv(),
        serverSettingsBuilder.toServerSettings()
    )

private fun <M, C, S> ServerSettingsBuilder<M, C, S>.toServerSettings() =
    ServerSettings(id, converters, url)
