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
import com.oliynick.max.elm.time.travel.exception.ConnectException
import com.oliynick.max.elm.time.travel.session.DebugSession
import com.oliynick.max.elm.time.travel.session.SessionBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import protocol.ComponentId
import protocol.NotifyComponentAttached
import protocol.NotifyComponentSnapshot
import protocol.NotifyServer
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
    Component(Dependencies(id, Env(initializer, resolver, update), config))

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
