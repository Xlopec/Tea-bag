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

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.component.internal.into
import com.oliynick.max.tea.core.component.internal.shareConflated
import com.oliynick.max.tea.core.debug.component.internal.mergeWith
import com.oliynick.max.tea.core.debug.protocol.*
import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Creates new debuggable [component][Component]
 *
 * @param id component identifier
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param jsonConverter json converter
 * @param config block to configure component
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
inline fun <reified M, reified C, reified S, J> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline updater: Updater<M, S, C>,
    jsonConverter: JsonConverter<J>,
    noinline config: DebugEnvBuilder<M, S, C, J>.() -> Unit = {},
): Component<M, S, C> =
    Component(Dependencies(id, EnvBuilder(initializer, resolver, updater), jsonConverter, config))

/**
 * Creates new component using preconfigured debug environment
 *
 * @param env environment to be used
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
fun <M, S, C, J> Component(
    env: DebugEnv<M, S, C, J>,
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input)

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
        .shareConflated()
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
