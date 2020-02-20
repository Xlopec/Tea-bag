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
@file:UseExperimental(InternalComponentApi::class)

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Update
import com.oliynick.max.tea.core.component.internal.*
import com.oliynick.max.tea.core.debug.exception.ConnectException
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.SessionBuilder
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

@PublishedApi
internal fun <M, C, S> DebugEnv<M, C, S>.upstream(
    input: Flow<M>
) = channelFlow {

    sessionBuilder(serverSettings) {
        @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
        componentEnv.upstream(input.mergeWith(messages), init().mergeWith(states.asSnapshots()))
            .onEach { snapshot -> notifyServer(this@sessionBuilder, snapshot) }
            .into(channel)
    }
}.catch { th -> notifyConnectException(serverSettings, th) }
    .shareConflated()

inline fun <reified M, reified C, reified S> Component(
    env: DebugEnv<M, C, S>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input.consumeAsFlow())

    return { messages -> upstream.downstream(messages, input) }
}

@PublishedApi
internal inline val <M, S> DebugEnv<M, *, S>.sessionBuilder: SessionBuilder<M, S>
    get() = serverSettings.sessionBuilder

@PublishedApi
internal fun notifyConnectException(
    serverSettings: ServerSettings<*, *>,
    th: Throwable
): Nothing =
    throw ConnectException(connectionFailureMessage(serverSettings), th)

private fun <S> Flow<S>.asSnapshots(): Flow<Initial<S, Nothing>> =
    // fixme what if Initial isn't what we're looking for?
    map { s -> Initial(s, emptySet()) }

private suspend fun <M, C, S> DebugEnv<M, C, S>.notifyServer(
    session: DebugSession<M, S>,
    snapshot: Snapshot<M, S, C>
) = with(serverSettings.serializer) {
    val message = when (snapshot) {
        // says 'hello' to a server; the message will be suspended until
        // the very first state gets computed
        is Initial -> NotifyComponentAttached(toJsonTree(snapshot.currentState))
        // observes state changes and notifies server about them
        is Regular -> NotifyComponentSnapshot(
            toJsonTree(snapshot.message),
            toJsonTree(snapshot.currentState),
            toJsonTree(snapshot.currentState)
        )
    }

    session.send(NotifyServer(UUID.randomUUID(), serverSettings.id, message))
}

private fun <T> Flow<T>.mergeWith(
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

private fun <M, S, C> DebugEnv<M, C, S>.init(): Flow<Initial<S, C>> = componentEnv.init()
