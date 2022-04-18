/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

@file:Suppress("FunctionName", "KDocUnresolvedReference")

package io.github.xlopec.tea.core.debug.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.debug.protocol.*
import io.github.xlopec.tea.data.RandomUUID
import io.github.xlopec.tea.core.*
import io.github.xlopec.tea.core.debug.component.internal.mergeWith
import io.github.xlopec.tea.core.debug.session.*
import io.ktor.http.*
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.core.debug.protocol.JsonSerializer
import io.github.xlopec.tea.core.debug.protocol.NotifyComponentAttached
import io.github.xlopec.tea.core.debug.protocol.NotifyComponentSnapshot
import io.github.xlopec.tea.core.debug.protocol.NotifyServer
import kotlinx.coroutines.CoroutineScope
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
 * @param jsonSerializer json converter
 * @param scope scope in which the sharing coroutine is started
 * @param url url used to connect to debug server
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param sessionFactory function that for a given server settings creates a new connection
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
    scope: CoroutineScope,
    // todo: group to reduce number of arguments
    url: Url = Localhost,
    jsonSerializer: JsonSerializer<J>,
    // see https://youtrack.jetbrains.com/issue/KT-47195
    // see https://github.com/Kotlin/kotlinx.coroutines/issues/3005#issuecomment-1014577573
    noinline sessionFactory: SessionFactory<M, S, J> = { settings, block -> HttpClient.session(settings, block) },
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
): Component<M, S, C> =
    Component(
        DebugEnv(
            Env(initializer, resolver, updater, scope, shareOptions),
            Settings(id, jsonSerializer, url, sessionFactory)
        )
    )

/**
 * Creates new component using preconfigured debug environment
 *
 * @param debugEnv environment to be used
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public fun <M, S, C, J> Component(
    debugEnv: DebugEnv<M, S, C, J>,
): Component<M, S, C> {

    val input = Channel<M>(RENDEZVOUS)
    val upstream = debugEnv.toComponentFlow(input)
        .shareIn(debugEnv.env.scope, debugEnv.env.shareOptions)

    return { messages -> upstream.withMessageCollector(messages, input::send) }
}

private fun <M, S, C, J> DebugEnv<M, S, C, J>.toComponentFlow(
    input: Channel<M>,
): Flow<Snapshot<M, S, C>> =
    debugSession { sink ->

        val initialSnapshots = mergeInitialSnapshots(this@toComponentFlow, states)
        val messagesForSnapshot =
            messagesForInitialSnapshot(input.receiveAsFlow(), this@toComponentFlow, messages)

        env.toComponentFlow(initialSnapshots, input::send, messagesForSnapshot)
            .onEach { snapshot -> notifyServer(this@toComponentFlow, snapshot) }
            .collect(sink::invoke)
    }

// todo refactor when multi-receivers KEEP is ready
private fun <M, S, C, J> mergeInitialSnapshots(
    debugEnv: DebugEnv<M, S, C, J>,
    debugStates: Flow<S>,
) = debugEnv.env.initial().mergeWith(debugStates.toSnapshots())

// todo refactor when multi-receivers KEEP is ready
private fun <M, S, C, J> messagesForInitialSnapshot(
    input: Flow<M>,
    debugEnv: DebugEnv<M, S, C, J>,
    debugMessages: Flow<M>,
): (Initial<S, C>) -> Flow<M> = { initial ->
    debugEnv.env.resolveAsFlow(initial.commands)
        .mergeWith(input)
        .mergeWith(debugMessages)
}

// todo refactor when multi-receivers KEEP is ready
private fun <M, S, C, J> DebugEnv<M, S, C, J>.debugSession(
    block: suspend DebugSession<M, S, J>.(input: Sink<Snapshot<M, S, C>>) -> Unit,
): Flow<Snapshot<M, S, C>> =
    channelFlow { settings.sessionFactory(settings) { block(channel::send) } }

private fun <S> Flow<S>.toSnapshots(): Flow<Initial<S, Nothing>> =
    // TODO what if we want to start from Regular snapshot?
    map { s -> Initial(s, setOf()) }

/**
 * Notifies server about state changes
 */
private suspend fun <M, S, C, J> DebugSession<M, S, J>.notifyServer(
    env: DebugEnv<M, S, C, J>,
    snapshot: Snapshot<M, S, C>,
) = with(env.settings) {
    invoke(NotifyServer(RandomUUID(), id, serializer.toServerMessage(snapshot)))
}

private fun <M, S, C, J> JsonSerializer<J>.toServerMessage(
    snapshot: Snapshot<M, S, C>,
) = when (snapshot) {
    is Initial -> NotifyComponentAttached(toJsonTree(snapshot.currentState), toCommandSet(snapshot.commands))
    is Regular -> NotifyComponentSnapshot(
        toJsonTree(snapshot.message),
        toJsonTree(snapshot.previousState),
        toJsonTree(snapshot.currentState),
        toCommandSet(snapshot.commands),
    )
}

private fun <C, J> JsonSerializer<J>.toCommandSet(
    s: Set<C>
): Set<J> = s.mapTo(HashSet(s.size), ::toJsonTree)
