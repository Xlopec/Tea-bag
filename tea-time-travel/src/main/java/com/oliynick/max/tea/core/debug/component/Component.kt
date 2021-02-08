/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.component.internal.into
import com.oliynick.max.tea.core.debug.component.internal.mergeWith
import com.oliynick.max.tea.core.debug.protocol.*
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.Localhost
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import com.oliynick.max.tea.core.debug.session.WebSocketSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import java.net.URL
import java.util.*

/**
 * Creates new debuggable [component][Component]
 *
 * @param id component identifier
 * @param initializer initializer that provides initial values
 * @param resolver resolver that resolves messages to commands and performs side effects
 * @param updater updater that computes new states and commands to be executed
 * @param jsonConverter json converter
 * @param scope scope in which the sharing coroutine is started
 * @param url url used to connect to debug server
 * @param io coroutine dispatcher which is used to execute side effects
 * @param computation coroutine dispatcher which is used to wrap [updater]'s computations
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param sessionBuilder function that for a given server settings creates a new connection
 * to a debug server
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public inline fun <reified M, reified C, reified S, J> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline updater: Updater<M, S, C>,
    jsonConverter: JsonConverter<J>,
    // todo: group to reduce number of arguments
    scope: CoroutineScope,
    url: URL = Localhost,
    io: CoroutineDispatcher = Dispatchers.IO,
    computation: CoroutineDispatcher = Dispatchers.Unconfined,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
    noinline sessionBuilder: SessionBuilder<M, S, J> = ::WebSocketSession,
): Component<M, S, C> =
    Component(
        DebugEnv(
            Env(initializer, resolver, updater, scope, io, computation, shareOptions),
            ServerSettings(id, jsonConverter, url, sessionBuilder))
    )

/**
 * Creates new component using preconfigured debug environment
 *
 * @param env environment to be used
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public fun <M, S, C, J> Component(
    env: DebugEnv<M, S, C, J>,
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input)
        .shareIn(env.componentEnv.scope, env.componentEnv.shareOptions)

    return { messages -> upstream.downstream(messages, input) }
}

private fun <M, S, C, J> DebugEnv<M, S, C, J>.upstream(
    input: Channel<M>,
): Flow<Snapshot<M, S, C>> {

    fun DebugSession<M, S, J>.inputFlow(): (Initial<S, C>) -> Flow<M> = { initial ->
        componentEnv.resolveAsFlow(initial.commands)
            .mergeWith(input.receiveAsFlow())
            .mergeWith(messages)
    }

    fun DebugSession<M, S, J>.debugUpstream() =
        componentEnv.upstream(init().mergeWith(states.asSnapshots()), input::send, inputFlow())
            .onEach { snapshot -> notifyServer(this, snapshot) }

    return session { inputChan -> debugUpstream().into(inputChan) }
}

@Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
private fun <M, S, C, J> DebugEnv<M, S, C, J>.session(
    block: suspend DebugSession<M, S, J>.(input: SendChannel<Snapshot<M, S, C>>) -> Unit,
): Flow<Snapshot<M, S, C>> =
    channelFlow { serverSettings.sessionBuilder(serverSettings) { block(channel) } }

private fun <S> Flow<S>.asSnapshots(): Flow<Initial<S, Nothing>> =
    // TODO what if we want to start from Regular snapshot?
    map { s -> Initial(s, emptySet()) }

/**
 * Notifies server about state changes
 */
private suspend fun <M, S, C, J> DebugEnv<M, S, C, J>.notifyServer(
    session: DebugSession<M, S, J>,
    snapshot: Snapshot<M, S, C>,
) = with(serverSettings) {
    session(
        NotifyServer(
            UUID.randomUUID(),
            id,
            serializer.toServerMessage(snapshot)
        )
    )
}

private fun <M, S, C, J> JsonConverter<J>.toServerMessage(
    snapshot: Snapshot<M, S, C>,
) = when (snapshot) {
    is Initial -> NotifyComponentAttached(toJsonTree(snapshot.currentState))
    is Regular -> NotifyComponentSnapshot(
        toJsonTree(snapshot.message),
        toJsonTree(snapshot.previousState),
        toJsonTree(snapshot.currentState)
    )
}

private fun <S, C> DebugEnv<*, S, C, *>.init(): Flow<Initial<S, C>> = componentEnv.init()
