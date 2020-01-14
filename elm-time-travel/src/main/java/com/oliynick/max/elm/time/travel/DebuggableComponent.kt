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
import com.oliynick.max.elm.core.loop.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
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
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import protocol.*
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicReference

//TODO refactor internal api!

interface DebugSession<M, S> {

    val messages: Flow<M>
    val states: Flow<S>

    suspend fun send(packet: NotifyServer)

}

typealias SessionBuilder<M, S> = suspend (ServerSettings) -> DebugSession<M, S>

suspend inline fun <reified M, reified S> WebSocketSession(
    settings: ServerSettings
): DebugSession<M, S> =
    channelFlow<DebugSession<M, S>> {
        httpClient.ws(
            method = HttpMethod.Get,
            host = settings.url.host,
            port = settings.url.port,
            block = { send(DebugWebSocketSession(M::class.java, S::class.java, settings, this)) }
        )
    }.first()

@PublishedApi
internal class DebugWebSocketSession<M, S>(
    private val mClass: Class<M>,
    private val sClass: Class<S>,
    private val settings: ServerSettings,
    private val socketSession: DefaultClientWebSocketSession
) : DebugSession<M, S> {

    private val incomingPackets: Flow<Either<M, S>> by unsafeLazy { settings.incomingCommands(socketSession) }

    override val messages: Flow<M> by unsafeLazy { incomingPackets.externalMessages() }
    override val states: Flow<S> by unsafeLazy { incomingPackets.externalStates() }

    override suspend fun send(packet: NotifyServer) =
        socketSession.send(settings.serializer.toJson(packet))

    private fun ServerSettings.incomingCommands(
        session: ClientWebSocketSession
    ) = incomingPackets(session)
        .map { packet -> serializer.toCommand(packet) }

    private fun JsonConverter.toCommand(
        packet: NotifyClient
    ) = when (val message = packet.message) {
        is ApplyMessage -> Left(
            fromJsonTree(
                message.message,
                mClass
            )
        )
        is ApplyState -> Right(
            fromJsonTree(
                message.state,
                sClass
            )
        )
    }

    private fun <T> unsafeLazy(
        provider: () -> T
    ) = lazy(LazyThreadSafetyMode.NONE, provider)

}

/*suspend inline fun <reified M, reified S> DebugEnv<M, *, S>.session(
    crossinline block: DebugSession<M, S>.() -> Unit
) = httpClient.ws(
    method = HttpMethod.Get,
    host = serverSettings.url.host,
    port = serverSettings.url.port,
    block = { block(DebugWebSocketSession(M::class.java, S::class.java, serverSettings, this)) }
)*/

@PublishedApi
internal val httpClient by lazy { HttpClient { install(WebSockets) } }
@PublishedApi
internal val localhost by lazy(::URL)

@DslMarker
private annotation class DslBuilder

//todo add dsl
data class DebugEnv<M, C, S>(
    inline val componentEnv: Env<M, C, S>,
    inline val serverSettings: ServerSettings,
    val sessionBuilder: SessionBuilder<M, S>
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
    debugEnv: DebugEnv<M, C, S>
): Component<M, S> {

    val (messages, states) = webSocketComponent(debugEnv)

    return newComponent(states, messages)
}

@PublishedApi
internal inline fun <reified M, reified C, reified S> CoroutineScope.webSocketComponent(
    env: DebugEnv<M, C, S>
): ComponentInternal<M, S> {

    val messages = Channel<M>()
    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    launch {
        try {
            connect(messages, statesChannel, env)
        } catch (th: Exception) {
            throw RuntimeException(
                "Failed to connect " +
                    "to the host: ${env.serverSettings.url}", th
            )
        }
    }

    return messages to statesChannel.asFlow()
}

@PublishedApi
internal suspend inline fun <reified M, reified C, reified S> connect(
    messages: Channel<M>,
    statesChannel: BroadcastChannel<S>,
    env: DebugEnv<M, C, S>
) =
    httpClient.ws(
        HttpMethod.Get,
        env.serverSettings.url.host,
        env.serverSettings.url.port
    ) {

        val snapshots = Channel<NotifyComponentSnapshot>()

        with(
            env.withSpyingInterceptor(
                snapshots,
                env.serverSettings.serializer
            )
        ) {
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

sealed class Either<out L, out R>

data class Left<L>(
    val l: L
) : Either<L, Nothing>()

data class Right<R>(
    val r: R
) : Either<Nothing, R>()

// todo implement a better naming
inline fun <reified M, reified C, reified S> DebugEnv<M, C, S>.snapshotComponent(): Component1<M, S, C> {

    val snapshots = AtomicReference<Snapshot<M, S, C>>()

    return { input ->

        channelFlow {

            with(sessionBuilder(serverSettings)) {

                componentEnv.begin(snapshots::get).mergeWith(states.map { s -> Initial(s, emptySet()) })
                    .flatMapConcat { snapshot ->
                        componentEnv.compute(
                            input.mergeWith(messages),
                            snapshot,
                            snapshots::set
                        )
                    }
                    .onEach { snapshot -> notifyServer(this@with, snapshot) }
                    .collect { send(it) }
            }

        }.catch { th -> notifyConnectException(serverSettings, th) }
    }
}

@PublishedApi
internal fun notifyConnectException(
    serverSettings: ServerSettings,
    th: Throwable
): Nothing =
    throw ConnectException(connectFailureMessage(serverSettings), th)

@PublishedApi
internal fun connectFailureMessage(
    serverSettings: ServerSettings
) = "Component '${serverSettings.id.id}' " +
    "couldn't connect to the endpoint ${serverSettings.url.toExternalForm()}"

@PublishedApi
internal fun <S> Flow<Either<*, S>>.externalStates(): Flow<S> =
    filterIsInstance<Right<S>>().map { (s) -> s }

@PublishedApi
internal fun <M> Flow<Either<M, *>>.externalMessages(): Flow<M> = filterIsInstance<Left<M>>().map { (m) -> m }

@PublishedApi
internal suspend inline fun <reified M, reified C, reified S> DebugEnv<M, C, S>.notifyServer(
    session: DebugSession<M, S>,
    snapshot: Snapshot<M, S, C>
) = with(serverSettings.serializer) {
    val message = when (snapshot) {
        // says 'hello' to a server; the message will be suspended until
        // the very first state gets computed
        is Initial -> NotifyComponentAttached(toJsonTree(snapshot.state))
        // observes state changes and notifies server about them
        is Regular -> NotifyComponentSnapshot(
            toJsonTree(snapshot.message),
            toJsonTree(snapshot.state),
            toJsonTree(snapshot.state)
        )
    }

    session.send(NotifyServer(UUID.randomUUID(), serverSettings.id, message))
}

@PublishedApi
internal suspend inline fun <reified M, C, reified S> ClientWebSocketSession.observeMessages(
    env: DebugEnv<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(env) {

        var computationJob = launch { componentEnv.loop(messages, states) }

        env.serverSettings.incomingPackets(this@observeMessages)
            .collect { packet ->
                // todo consider replacing with some kind of stream and fold function
                val newJob = applyMessage(packet.message, env, messages, states)

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
    env: DebugEnv<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
): Job? = with(env) {
    @Suppress("UNCHECKED_CAST")
    when (message) {
        // todo split into functions per message type
        is ApplyMessage -> {
            messages.send(
                env.serverSettings.serializer.fromJsonTree(
                    message.message,
                    M::class.java
                )
            )
            null
        }
        is ApplyState -> {
            // cancels previous computation job and starts a new one
            launch {
                loop<M, C, S>(
                    env.serverSettings.serializer.fromJsonTree(
                        message.state,
                        S::class.java
                    ),
                    this@with,
                    messages,
                    states
                )
            }
        }
    }
}

@PublishedApi
internal fun ServerSettings.incomingPackets(
    clientWebSocketSession: ClientWebSocketSession
) =
    clientWebSocketSession.incoming.broadcast().asFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> serializer.fromJson(json, NotifyClient::class.java) }
        .filter { packet -> packet.component == id }

@PublishedApi
internal inline fun <reified M, reified S> DebugEnv<M, *, S>.incomingCommands(
    session: ClientWebSocketSession
) = serverSettings.incomingPackets(session)
    .map { packet -> toCommand(packet) }

@PublishedApi
internal inline fun <reified M, reified S> DebugEnv<M, *, S>.toCommand(
    packet: NotifyClient
) = when (val message = packet.message) {
    is ApplyMessage -> Left(
        serverSettings.serializer.fromJsonTree(
            message.message,
            M::class.java
        )
    )
    is ApplyState -> Right(
        serverSettings.serializer.fromJsonTree(
            message.state,
            S::class.java
        )
    )
}

@PublishedApi
internal suspend inline fun <M, C, reified S> loop(
    stateValue: S,
    inputEnv: DebugEnv<M, C, S>,
    messages: Channel<M>,
    states: BroadcastChannel<S>
) {

    with(inputEnv) {
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
): LegacyInterceptor<M, S, C> = { message, prevState, newState, _ ->
    sink.send(
        NotifyComponentSnapshot(
            serializer.toJsonTree(message as Any),
            serializer.toJsonTree(prevState as Any),
            serializer.toJsonTree(newState as Any)
        )
    )
}

@PublishedApi
internal inline fun <reified M, reified C, reified S> DebugEnv<M, C, S>.withSpyingInterceptor(
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
    DebugEnv(
        dependenciesBuilder.toEnv(),
        serverSettingsBuilder.toServerSettings(),
        ::WebSocketSession
    )

@PublishedApi
internal fun ServerSettingsBuilder.toServerSettings() =
    ServerSettings(id, jsonSerializer, url)

fun <T> Flow<T>.mergeWith(
    another: Flow<T>
): Flow<T> =
    channelFlow {
        coroutineScope {
            launch {
                another.collect {
                    send(it)
                }
            }

            launch {
                collect {
                    send(it)
                }
            }
        }
    }