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

package com.oliynick.max.elm.time.travel.component

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.core.loop.*
import com.oliynick.max.elm.time.travel.converter.GsonSerializer
import com.oliynick.max.elm.time.travel.converter.JsonConverter
import com.oliynick.max.elm.time.travel.exception.ConnectException
import com.oliynick.max.elm.time.travel.session.*
import io.ktor.client.features.websocket.ClientWebSocketSession
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

//TODO refactor internal api!

fun URL(
    protocol: String = "http",
    host: String = "localhost",
    port: UInt = 8080U
) =
    URL(protocol, host, port.toInt(), "")

inline fun <reified M, reified C, reified S> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline update: Update<M, S, C>,
    noinline config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
): Component<M, S, C> =
    Component(Dependencies(id, Env(toLegacy(initializer), resolver, update), config))

private fun <M, S, C> Env<M, C, S>.doCompute(
    startFrom: Snapshot<M, S, C>,
    messages: Flow<M>
): Flow<Snapshot<M, S, C>> =
    compute(messages(startFrom.commands(), messages), startFrom)
        .startFrom(startFrom)

@PublishedApi
internal fun <M, S, C> Env<M, C, S>.upstream(
    messages: Flow<M>,
    snapshots: Flow<Snapshot<M, S, C>>
) = begin().mergeWith(snapshots)
    .flatMapConcat { startFrom ->
        doCompute(
            startFrom,
            messages
        )
    }

@PublishedApi
internal fun <M, C, S> DebugEnv<M, C, S>.debugSession(
    input: Flow<M>
) = channelFlow {

    sessionBuilder(serverSettings) {
        @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
        componentEnv.upstream(input.mergeWith(messages), states.asSnapshots())
            .onEach { snapshot -> notifyServer(this@sessionBuilder, snapshot) }
            .collectTo(channel)
    }
}.catch { th -> notifyConnectException(serverSettings, th) }
    .shareConflated()

inline fun <reified M, reified C, reified S> Component(
    env: DebugEnv<M, C, S>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val debugSession = env.debugSession(input.consumeAsFlow())

    return { messages ->

        channelFlow {
            // todo create `collect` extension for producer scope
            @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
            debugSession.onStart { launch { messages.collectTo(input) } }.collectTo(channel)
        }
    }
}

private fun <C> Snapshot<*, *, C>.commands() = if (this is Initial) commands else emptySet()

@PublishedApi
internal inline val <M, S> DebugEnv<M, *, S>.sessionBuilder: SessionBuilder<M, S>
    get() = serverSettings.sessionBuilder

@PublishedApi
internal fun notifyConnectException(
    serverSettings: ServerSettings<*, *>,
    th: Throwable
): Nothing =
    throw ConnectException(connectionFailureMessage(serverSettings), th)

@PublishedApi
internal fun <S> Flow<S>.asSnapshots(): Flow<Initial<S, Nothing>> =
    map { s -> Initial(s, emptySet()) }

@PublishedApi
internal suspend fun <M, C, S> DebugEnv<M, C, S>.notifyServer(
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
internal fun <T> Flow<T>.mergeWith(
    another: Flow<T>
): Flow<T> = channelFlow {
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

internal fun connectionFailureMessage(
    serverSettings: ServerSettings<*, *>
) = "Component '${serverSettings.id.id}' " +
    "couldn't connect to the endpoint ${serverSettings.url.toExternalForm()}"

///////////////////////
// Deprecated API
//////////////////////

@Deprecated("will be removed, use Component instead")
inline fun <reified M, reified C, reified S> CoroutineScope.ComponentLegacy(
    debugEnv: DebugEnv<M, C, S>
): ComponentLegacy<M, S> {

    val (messages, states) = webSocketComponent(debugEnv)

    return newComponent(states, messages)
}

@PublishedApi
@Deprecated("will be removed, use snapshotComponent instead")
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
@Deprecated("will be removed")
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


@Deprecated("will be removed")
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

@Deprecated("will be removed")
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

@Deprecated("will be removed")
@PublishedApi
internal fun <M, S> ServerSettings<M, S>.incomingPackets(
    clientWebSocketSession: ClientWebSocketSession
) =
    clientWebSocketSession.incoming.broadcast().asFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> serializer.fromJson(json, NotifyClient::class.java) }
        .filter { packet -> packet.component == id }

@Deprecated("will be removed")
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

@Deprecated("will be removed")
@PublishedApi
internal suspend inline fun <M, reified S> WebSocketSession.notifyAttached(
    first: S,
    serverSettings: ServerSettings<M, S>
) {
    val message = notifyMessage(
        NotifyComponentAttached(serverSettings.serializer.toJsonTree(first as Any)),
        serverSettings.id
    )

    send(serverSettings.serializer.toJson(message))
}

@Deprecated("will be removed")
@PublishedApi
internal suspend fun <M, S> WebSocketSession.notifySnapshots(
    snapshots: Flow<NotifyComponentSnapshot>,
    serverSettings: ServerSettings<M, S>
) {
    snapshots.collect { snapshot ->
        send(
            serverSettings.id,
            snapshot,
            serverSettings.serializer
        )
    }
}

@Deprecated("will be removed")
@PublishedApi
internal fun notifyMessage(
    message: ServerMessage,
    componentId: ComponentId,
    packetId: UUID = UUID.randomUUID()
) =
    NotifyServer(packetId, componentId, message)

@Deprecated("will be removed")
@PublishedApi
internal suspend fun WebSocketSession.send(
    componentId: ComponentId,
    message: ServerMessage,
    serializer: JsonConverter
) {
    send(serializer.toJson(NotifyServer(UUID.randomUUID(), componentId, message)))
}

@Deprecated("will be removed")
@PublishedApi
internal suspend fun WebSocketSession.notifyApplied(
    packetId: UUID,
    componentId: ComponentId,
    serializer: JsonConverter
) {
    send(serializer.toJson(NotifyServer(packetId, componentId, ActionApplied(packetId))))
}

@PublishedApi
@Deprecated("will be removed")
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
@Deprecated("will be removed")
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
@Deprecated("will be removed")
internal inline fun <reified M, reified C, reified S> Env<M, C, S>.withSpyingInterceptor(
    snapshots: Channel<NotifyComponentSnapshot>,
    serializer: JsonConverter
) = copy(
    interceptor = spyingInterceptor<M, C, S>(
        snapshots,
        serializer
    ).with(interceptor)
)

@PublishedApi
@Deprecated("will be removed")
internal fun <M, C, S> Env<M, C, S>.withNewInitializer(s: S) =
    copy(initializer = { s to emptySet() })

@PublishedApi
internal inline fun <reified M, reified C, reified S> DebugEnvBuilder<M, C, S>.toDebugDependencies() =
    DebugEnv(
        dependenciesBuilder.toEnv(),
        serverSettingsBuilder.toServerSettings()
    )

@PublishedApi
internal inline fun <reified M, reified S> ServerSettingsBuilder<M, S>.toServerSettings() =
    ServerSettings(
        id,
        jsonSerializer ?: GsonSerializer(),
        url ?: localhost,
        sessionBuilder ?: ::WebSocketSession
    )

