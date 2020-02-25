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
@file:UseExperimental(UnstableApi::class)

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Update
import com.oliynick.max.tea.core.component.internal.*
import com.oliynick.max.tea.core.debug.component.internal.mergeWith
import com.oliynick.max.tea.core.debug.exception.ConnectException
import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import protocol.*
import java.util.*

inline fun <reified M, reified C, reified S, J> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline update: Update<M, S, C>,
    jsonConverter: JsonConverter<J>,
    noinline config: DebugEnvBuilder<M, C, S, J>.() -> Unit = {}
): Component<M, S, C> =
    Component(Dependencies(id, Env(initializer, resolver, update), jsonConverter, config))

fun <M, C, S, J> Component(
    env: DebugEnv<M, C, S, J>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input.consumeAsFlow())

    return { messages -> upstream.downstream(messages, input) }
}

private fun <M, C, S, J> DebugEnv<M, C, S, J>.upstream(
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
private fun <M, S, C, J> DebugEnv<M, C, S, J>.session(
    block: suspend DebugSession<M, S, J>.(input: SendChannel<Snapshot<M, S, C>>) -> Unit
): Flow<Snapshot<M, S, C>> = channelFlow { serverSettings.sessionBuilder(serverSettings) { block(channel) } }

private fun <S> Flow<S>.asSnapshots(): Flow<Initial<S, Nothing>> =
    // TODO what if we want to start from Regular snapshot?
    map { s -> Initial(s, emptySet()) }

/**
 * Notifies server about state changes
 */
private suspend fun <M, C, S, J> DebugEnv<M, C, S, J>.notifyServer(
    session: DebugSession<M, S, J>,
    snapshot: Snapshot<M, S, C>
) = with(serverSettings) {
    session(NotifyServer(UUID.randomUUID(), id, serializer.toServerMessage(snapshot)))
}

private fun <M, C, S, J> JsonConverter<J>.toServerMessage(
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

private fun <M, S, C> DebugEnv<M, C, S, *>.init(): Flow<Initial<S, C>> = componentEnv.init()
