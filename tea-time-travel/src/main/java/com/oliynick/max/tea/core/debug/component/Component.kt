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
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.EnvBuilder
import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.UnstableApi
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Updater
import com.oliynick.max.tea.core.component.internal.downstream
import com.oliynick.max.tea.core.component.internal.init
import com.oliynick.max.tea.core.component.internal.into
import com.oliynick.max.tea.core.component.internal.shareConflated
import com.oliynick.max.tea.core.component.internal.upstream
import com.oliynick.max.tea.core.debug.component.internal.mergeWith
import com.oliynick.max.tea.core.debug.exception.ConnectException
import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import protocol.ComponentId
import protocol.JsonConverter
import protocol.NotifyComponentAttached
import protocol.NotifyComponentSnapshot
import protocol.NotifyServer
import java.util.*

inline fun <reified M, reified C, reified S, J> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline updater: Updater<M, S, C>,
    jsonConverter: JsonConverter<J>,
    noinline config: DebugEnvBuilder<M, S, C, J>.() -> Unit = {}
): Component<M, S, C> =
    Component(Dependencies(id, EnvBuilder(initializer, resolver, updater), jsonConverter, config))

fun <M, S, C, J> Component(
    env: DebugEnv<M, S, C, J>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input.consumeAsFlow())

    return { messages -> upstream.downstream(messages, input) }
}

private fun <M, S, C, J> DebugEnv<M, S, C, J>.upstream(
    input: Flow<M>
): Flow<Snapshot<M, S, C>> {

    fun DebugSession<M, S, J>.debugUpstream() =
        componentEnv.upstream(input.mergeWith(messages), init().mergeWith(states.asSnapshots()))
            .onEach { snapshot -> notifyServer(this, snapshot) }

    return session { inputChan -> debugUpstream().into(inputChan) }
        .catch { th -> notifyConnectException(serverSettings, th) }
        .shareConflated()
}

@Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
private fun <M, S, C, J> DebugEnv<M, S, C, J>.session(
    block: suspend DebugSession<M, S, J>.(input: SendChannel<Snapshot<M, S, C>>) -> Unit
): Flow<Snapshot<M, S, C>> = channelFlow { serverSettings.sessionBuilder(serverSettings) { block(channel) } }

private fun <S> Flow<S>.asSnapshots(): Flow<Initial<S, Nothing>> =
    // TODO what if we want to start from Regular snapshot?
    map { s -> Initial(s, emptySet()) }

/**
 * Notifies server about state changes
 */
private suspend fun <M, S, C, J> DebugEnv<M, S, C, J>.notifyServer(
    session: DebugSession<M, S, J>,
    snapshot: Snapshot<M, S, C>
) = with(serverSettings) {
    session(NotifyServer(UUID.randomUUID(), id, serializer.toServerMessage(snapshot)))
}

private fun <M, S, C, J> JsonConverter<J>.toServerMessage(
    snapshot: Snapshot<M, S, C>
) = when (snapshot) {
    is Initial -> NotifyComponentAttached(toJsonTree(snapshot.currentState))
    is Regular -> NotifyComponentSnapshot(
        toJsonTree(snapshot.message),
        toJsonTree(snapshot.previousState),
        toJsonTree(snapshot.currentState)
    )
}

private fun notifyConnectException(
    serverSettings: ServerSettings<*, *, *>,
    th: Throwable
): Nothing =
    throw ConnectException(connectionFailureMessage(serverSettings), th)

private fun connectionFailureMessage(
    serverSettings: ServerSettings<*, *, *>
) = "Component '${serverSettings.id.id}' " +
        "couldn't connect to the endpoint ${serverSettings.url.toExternalForm()}"

private fun <S, C> DebugEnv<*, S, C, *>.init(): Flow<Initial<S, C>> = componentEnv.init()
