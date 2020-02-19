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

package com.oliynick.max.elm.core.component

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

inline fun <reified M, reified C, reified S> Component(
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline update: Update<M, S, C>
): Component<M, S, C> = Component(Env(initializer, resolver, update))

fun <M, C, S> Component(
    env: Env<M, C, S>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)
    val upstream = env.upstream(input.consumeAsFlow(), env.init()).shareConflated()

    return { messages -> upstream.downstream(messages, input) }
}

fun <M, S, C> Env<M, C, S>.upstream(
    messages: Flow<M>,
    snapshots: Flow<Initial<S, C>>
) = snapshots.flatMapConcat { startFrom -> doCompute(startFrom, messages) }

fun <M, S, C> Flow<Snapshot<M, S, C>>.downstream(
    input: Flow<M>,
    upstreamInput: Channel<M>
): Flow<Snapshot<M, S, C>> = channelFlow {
    @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
    onStart { launch { input.collectTo(upstreamInput) } }
        .collectTo(channel)
}

fun <M, S, C> Env<M, C, S>.doCompute(
    startFrom: Initial<S, C>,
    messages: Flow<M>
): Flow<Snapshot<M, S, C>> =
    compute(messages(startFrom.commands, messages), startFrom)
        .startFrom(startFrom)

suspend fun <T> Flow<T>.collectTo(
    sendChannel: SendChannel<T>
) = collect(sendChannel::send)

private fun <M, C, S> Env<M, C, S>.messages(
    commands: Collection<C>,
    input: Flow<M>
) = flow { emitAll(resolver(commands).asFlow()) }
    .onCompletion { th -> if (th != null) throw th else emitAll(input) }

private fun <M, C, S> Env<M, C, S>.compute(
    messages: Flow<M>,
    startFrom: Snapshot<M, S, C>
): Flow<Snapshot<M, S, C>> =
    messages.foldFlatten(startFrom) { snapshot, message ->
        computeNextSnapshot(snapshot.state, message)
    }

private suspend fun <M, C, S> Env<M, C, S>.computeNextSnapshotsRecursively(
    state: S,
    messages: Iterator<M>
): Flow<Snapshot<M, S, C>> {

    val message = messages.nextOrNull() ?: return emptyFlow()

    val (nextState, commands) = update(message, state)

    return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
        .startFrom(Regular(nextState, commands, state, message))
}

private suspend fun <M, C, S> Env<M, C, S>.computeNextSnapshot(
    state: S,
    message: M
): Flow<Snapshot<M, S, C>> {
    // todo: we need to add possibility to return own versions
    //  of snapshots, e.g. user might be interested only in current
    //  version of state
    val (nextState, commands) = update(message, state)

    return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
        .startFrom(Regular(nextState, commands, state, message))
}

fun <M, S, C> Env<M, C, S>.init(): Flow<Initial<S, C>> =
    flow { emit(initializer()) }

private inline fun <T, R> Flow<T>.foldFlatten(
    acc: R,
    crossinline transform: suspend (R, T) -> Flow<R>
): Flow<R> {

    var current = acc

    return flatMapConcat { next ->
        transform(current, next)
            .onEach { new -> current = new }
    }
}

private fun <T> Flow<T>.startFrom(
    t: T
) = onStart { emit(t) }

private fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

private suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> =
    commands.fold(LinkedHashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }

