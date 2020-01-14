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

package com.oliynick.max.elm.core.loop

import com.oliynick.max.elm.core.component.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

/**
 * Internal alias of a component
 */
typealias ComponentInternal<M, S> = Pair<SendChannel<M>, Flow<S>>

/**
 * Stores a new state to channel and notifies subscribers about changes
 */
suspend fun <M, C, S> Env<M, C, S>.updateMutating(
    message: M,
    state: S,
    states: BroadcastChannel<S>
): UpdateWith<S, C> =
    update(message, state)
        // we don't want to suspend here
        .also { (nextState, _) -> states.offerChecking(nextState) }
        .also { (nextState, commands) -> interceptor(message, state, nextState, commands) }

/**
 * Polls messages from channel's iterator and computes subsequent component's states.
 * Before polling a message from the channel it tries to computes all
 * subsequent states produced by resolved commands
 */
tailrec suspend fun <M, C, S> Env<M, C, S>.loop(
    state: S,
    it: ChannelIterator<M>,
    states: BroadcastChannel<S>
): S {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = updateMutating(message, state, states)

    return loop(
        loop(
            nextState,
            resolver(commands).iterator(),
            states
        ),
        it,
        states
    )
}

/**
 * Polls messages from collection's iterator and computes next states until collection is empty
 */
suspend fun <M, C, S> Env<M, C, S>.loop(
    state: S,
    it: Iterator<M>,
    states: BroadcastChannel<S>
): S {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = updateMutating(message, state, states)

    return loop(
        loop(
            nextState,
            it,
            states
        ),
        resolver(commands).iterator(),
        states
    )
}

/**
 * Loads an initial state using supplied initializer and starts component's loop
 */
suspend fun <M, C, S> Env<M, C, S>.loop(
    messages: Channel<M>,
    states: BroadcastChannel<S>
): S {

    val (initState, initCommands) = initializer()
        .also { (initialState, _) -> states.offerChecking(initialState) }

    val nonTransient =
        loop(initState, resolver(initCommands).iterator(), states)

    return loop(nonTransient, messages.iterator(), states)
}

fun <M, C, S> Env<M, C, S>.snapshotComponent(): Component1<M, S, C> {

    val snapshots = AtomicReference<Snapshot<M, S, C>>()

    return { input ->

        channelFlow {

            launch {
                begin(snapshots::get)
                    .flatMapConcat { snapshot -> compute(input, snapshot, snapshots::set) }
                    .collect { send(it) }
            }
        }
    }
}

fun <M, C, S> Env<M, C, S>.begin(
    store: () -> Snapshot<M, S, C>?
) = (store()?.let(::flowOf) ?: init())

fun <M, C, S> Env<M, C, S>.compute(
    messages: Flow<M>,
    startFrom: Snapshot<M, S, C>,
    sink: (Snapshot<M, S, C>) -> Unit
) = computeSnapshots(messages, startFrom)
    .flatMapConcat { snapshot -> computeSnapshots(resolver(snapshot.commands).asFlow(), snapshot) }
    .onEach { sink(it) }

fun <M, S, C> Env<M, C, S>.init(): Flow<Initial<S, C>> =
    flow { emit(initializer()) }
        .map { (initState, initCommands) -> Initial(initState, initCommands) }

private fun <M, S, C> Env<M, C, S>.computeSnapshots(
    messages: Flow<M>,
    acc: Snapshot<M, S, C>
) = messages.scan(acc) { snapshot, message ->

    val (nextState, commands) = update(message, snapshot.state)

    Regular(message, nextState, commands)
}

/**
 * Combines given flow of states and message channel into TEA component
 */
fun <M, S> newComponent(state: Flow<S>, messages: SendChannel<M>): Component<M, S> =
    { input ->

        channelFlow {

            launch {
                state.distinctUntilChanged().collect { state ->
                    send(state)
                }
            }

            launch {
                input.collect { message ->
                    messages.sendChecking(message)
                }
            }
        }
    }

fun <E> BroadcastChannel<E>.offerChecking(e: E) =
    check(offer(e)) { "Couldn't offer next element - $e" }

fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

suspend fun <E> ChannelIterator<E>.nextOrNull() = if (hasNext()) next() else null

suspend fun <E> SendChannel<E>.sendChecking(e: E) {
    check(!isClosedForSend) { "Component was already disposed" }
    send(e)
}

suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> {
    return commands.fold(HashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }
}

val Unit?.safe get() = this