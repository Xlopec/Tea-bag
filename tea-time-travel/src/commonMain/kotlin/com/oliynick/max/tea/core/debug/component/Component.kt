/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.entities.shared.randomUUID
import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.debug.component.internal.mergeWith
import com.oliynick.max.tea.core.debug.protocol.*
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.Localhost
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import com.oliynick.max.tea.core.debug.session.WebSocketSession
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.*

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
 * @param computation coroutine dispatcher which is used to wrap [updater]'s computations
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param sessionBuilder function that for a given server settings creates a new connection
 * to a debug server
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public inline fun <reified M : Any, reified C, reified S : Any, J> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline updater: Updater<M, S, C>,
    jsonConverter: JsonConverter<J>,
    // todo: group to reduce number of arguments
    scope: CoroutineScope,
    url: Url = Localhost,
    computation: CoroutineDispatcher = Dispatchers.Unconfined,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
    noinline sessionBuilder: SessionBuilder<M, S, J> = ::WebSocketSession,
): Component<M, S, C> =
    Component(
        DebugEnv(
            Env(initializer, resolver, updater, scope, computation, shareOptions),
            ServerSettings(id, jsonConverter, url, sessionBuilder)
        )
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

    val input = Channel<M>(RENDEZVOUS)
    val upstream = env.toComponentFlow(input)
        .shareIn(env.componentEnv.scope, env.componentEnv.shareOptions)

    return { messages -> upstream.withMessageCollector(messages, input::send) }
}

private fun <M, S, C, J> DebugEnv<M, S, C, J>.toComponentFlow(
    input: Channel<M>,
): Flow<Snapshot<M, S, C>> =
    debugSession { sink ->
        componentEnv.toComponentFlow(
            mergeInitialSnapshots(this@toComponentFlow, this@debugSession),
            input::send,
            messagesForInitialSnapshot(input.receiveAsFlow(), this@toComponentFlow, this@debugSession)
        )
            .onEach { snapshot -> notifyServer(this@debugSession, snapshot) }
            .collect(sink::invoke)
    }

// todo refactor when multi-receivers KEEP is ready
private fun <M, S, C, J> mergeInitialSnapshots(
    env: DebugEnv<M, S, C, J>,
    session: DebugSession<M, S, J>,
) = env.componentEnv.initial().mergeWith(session.states.asSnapshots())

// todo refactor when multi-receivers KEEP is ready
private fun <M, S, C, J> messagesForInitialSnapshot(
    input: Flow<M>,
    env: DebugEnv<M, S, C, J>,
    session: DebugSession<M, S, J>,
): (Initial<S, C>) -> Flow<M> = { initial ->
    env.componentEnv.resolveAsFlow(initial.commands)
        .mergeWith(input)
        .mergeWith(session.messages)
}

@Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
private fun <M, S, C, J> DebugEnv<M, S, C, J>.debugSession(
    block: suspend DebugSession<M, S, J>.(input: Sink<Snapshot<M, S, C>>) -> Unit,
): Flow<Snapshot<M, S, C>> =
    channelFlow { serverSettings.sessionBuilder(serverSettings) { block(channel::send) } }

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
            randomUUID(),
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
