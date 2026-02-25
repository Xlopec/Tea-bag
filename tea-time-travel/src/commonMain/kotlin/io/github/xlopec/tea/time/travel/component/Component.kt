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

package io.github.xlopec.tea.time.travel.component

import io.github.xlopec.tea.core.*
import io.github.xlopec.tea.time.travel.component.internal.mergeWith
import io.github.xlopec.tea.time.travel.protocol.*
import io.github.xlopec.tea.time.travel.session.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.*
import kotlin.uuid.Uuid

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
public inline fun <reified M : Any, reified S : Any, reified C, J> Component(
    id: ComponentId,
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<M, S, C>,
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
    val upstream = debugEnv.computeSnapshots(input)
        .shareIn(debugEnv.env.scope, debugEnv.env.shareOptions)

    return { messages -> upstream.attachMessageCollector(messages, input::send) }
}

private fun <M, S, C, J> DebugEnv<M, S, C, J>.computeSnapshots(
    input: Channel<M>,
): Flow<Snapshot<M, S, C>> =
    debugSession { sink ->
        env.computeSnapshots(mergeInitialSnapshots(states), input::send, mergeMessages(input.receiveAsFlow(), messages))
            .onEach { snapshot -> notifyServer(this@computeSnapshots, snapshot) }
            .collect(sink::invoke)
    }

private fun <M, S, C, J> DebugEnv<M, S, C, J>.mergeInitialSnapshots(
    debugStates: Flow<S>,
) = env.initial().mergeWith(debugStates.toInitialSnapshots())

private fun <M, S, C, J> DebugEnv<M, S, C, J>.mergeMessages(
    originalInput: Flow<M>,
    debugInput: Flow<M>,
): (Initial<S, C>) -> Flow<M> = { initial ->
    env.resolveAsFlow(initial)
        .mergeWith(originalInput)
        .mergeWith(debugInput)
}

private fun <M, S, C, J> DebugEnv<M, S, C, J>.debugSession(
    block: suspend DebugSession<M, S, J>.(input: Sink<Snapshot<M, S, C>>) -> Unit,
): Flow<Snapshot<M, S, C>> =
    channelFlow { settings.sessionFactory(settings) { block(channel::send) } }

private fun <S> Flow<S>.toInitialSnapshots(): Flow<Initial<S, Nothing>> =
    // TODO what if we want to start from Regular snapshot?
    map { s -> Initial(s, setOf()) }

/**
 * Notifies server about state changes
 */
// TODO context(DebugSession<M, S, J>, DebugEnv<M, S, C, J>)
private suspend fun <M, S, C, J> DebugSession<M, S, J>.notifyServer(
    env: DebugEnv<M, S, C, J>,
    snapshot: Snapshot<M, S, C>,
) = with(env.settings) {
    invoke(NotifyServer(Uuid.random(), id, serializer.toServerMessage(snapshot)))
}

private fun <M, S, C, J> JsonSerializer<J>.toServerMessage(
    snapshot: Snapshot<M, S, C>,
) = when (snapshot) {
    is Initial -> NotifyComponentAttached(toJsonTree(snapshot.currentState), toCommandsSet(snapshot.commands))
    is Regular -> NotifyComponentSnapshot(
        toJsonTree(snapshot.message),
        toJsonTree(snapshot.previousState),
        toJsonTree(snapshot.currentState),
        toCommandsSet(snapshot.commands),
    )
}

private fun <C, J> JsonSerializer<J>.toCommandsSet(
    s: Set<C>,
): Set<J> = s.mapTo(HashSet(s.size), ::toJsonTree)
