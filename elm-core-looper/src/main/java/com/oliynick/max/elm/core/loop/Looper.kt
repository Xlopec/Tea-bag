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

package com.oliynick.max.elm.core.loop

import com.oliynick.max.elm.core.component.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Internal alias of a component
 */
@Deprecated("will be removed")
typealias ComponentInternal<M, S> = Pair<SendChannel<M>, Flow<S>>

/**
 * Stores a new state to channel and notifies subscribers about changes
 */
@Deprecated("will be removed")
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
@Deprecated("will be removed")
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
@Deprecated("will be removed")
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
@Deprecated("will be removed")
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

inline fun <reified M, reified C, reified S> ComponentFock(
    noinline initializer: Initializer<S, C>,
    noinline resolver: Resolver<C, M>,
    noinline update: Update<M, S, C>
): Component<M, S, C> = ComponentFock(
    Env(
        initializer = toLegacy(initializer),
        resolver = resolver,
        update = update
    )
)

fun <S, C> toLegacy(
    initializer: Initializer<S, C>
): InitializerLegacy<S, C> = { initializer().let { (s, c) -> s to c } }

fun <M, C, S> ComponentFock(
    env: Env<M, C, S>
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)

    fun Env<M, C, S>.doCompute(
        initial: Initial<S, C>
    ) = compute(messages(initial, input.consumeAsFlow()), initial).startFrom(initial)

    val upstream =
        with(env) {
            beginNew().flatMapConcat { initial -> doCompute(initial) }.shareConflated()
        }

    return { messages ->

        channelFlow {

            coroutineScope {

                val messageJob = launch(start = CoroutineStart.LAZY) {
                    messages.collectTo(input)
                }

                launch {
                    @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
                    upstream
                        .onStart { messageJob.start() }
                        .collectTo(channel)
                }
            }
        }
    }
}

suspend fun <T> Flow<T>.collectTo(
    sendChannel: SendChannel<T>
) = collect(sendChannel::send)

fun <M, C, S> Env<M, C, S>.messages(
    initial: Initial<S, C>,
    input: Flow<M>
) = messages(initial.commands, input)

fun <M, C, S> Env<M, C, S>.messages(
    commands: Collection<C>,
    input: Flow<M>
) = flow { emitAll(resolver(commands).asFlow()) }
    .onCompletion { th -> if (th != null) throw th else emitAll(input) }

fun <M, C, S> Env<M, C, S>.beginNew(): Flow<Initial<S, C>> =
    flow { emit(newInitializer()) }

fun <M, C, S> Env<M, C, S>.begin(
    channel: BroadcastChannel<Snapshot<M, S, C>>,
    mutex: Mutex
): Flow<Snapshot<M, S, C>> =
    flow { emit(load(channel, mutex)) }

private suspend fun <M, C, S> Env<M, C, S>.load(
    channel: BroadcastChannel<Snapshot<M, S, C>>,
    mutex: Mutex
) = mutex.withLock {
    var latest = channel.latest()

    if (latest == null) {
        latest = newInitializer()
        channel.send(latest)
    }

    latest
}

fun <E> BroadcastChannel<E>.latest() = openSubscription().poll()

fun <M, C, S> Env<M, C, S>.compute(
    messages: Flow<M>,
    startFrom: Snapshot<M, S, C>
): Flow<Snapshot<M, S, C>> =
    messages.foldFlatten(startFrom) { snapshot, message ->
        computeNextSnapshot(snapshot, message)
    }

suspend fun <M, C, S> Env<M, C, S>.computeNextSnapshotsRecursively(
    state: S,
    messages: Iterator<M>
): Flow<Snapshot<M, S, C>> {

    val message = messages.nextOrNull() ?: return emptyFlow()

    val (nextState, commands) = update(message, state)

    return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
        .startFrom(Regular(message, nextState, commands))
}

suspend fun <M, C, S> Env<M, C, S>.computeNextSnapshot(
    snapshot: Snapshot<M, S, C>,
    message: M
): Flow<Snapshot<M, S, C>> {
    // todo: we need to add possibility to return own versions
    //  of snapshots, e.g. user might be interested only in current
    //  version of state
    val (nextState, commands) = update(message, snapshot.state)

    return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
        .startFrom(Regular(message, nextState, commands))
}

@ObsoleteComponentApi
fun <M, C, S> Env<M, C, S>.compute(
    messages: Flow<M>,
    startFrom: Snapshot<M, S, C>,
    sink: (Snapshot<M, S, C>) -> Unit
): Flow<Snapshot<M, S, C>> {

    suspend fun computeNextSnapshotsRecursively(
        state: S,
        messages: Iterator<M>
    ): Flow<Regular<M, S, C>> {

        val message = messages.nextOrNull() ?: return emptyFlow()

        val (nextState, commands) = update(message, state)

        return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
            .startFrom(Regular(message, nextState, commands))
    }

    suspend fun computeNextSnapshot(
        snapshot: Snapshot<M, S, C>,
        message: M
    ): Flow<Regular<M, S, C>> {
        // todo: we need to add possibility to return own versions
        //  of snapshots, e.g. user might be interested only in current
        //  version of state
        val (nextState, commands) = update(message, snapshot.state)

        return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
            .startFrom(Regular(message, nextState, commands))
    }

    return messages
        .onStart { emitAll(resolver(startFrom.initialCommands()).asFlow()) }
        .foldFlatten(startFrom, ::computeNextSnapshot)
        .onEach { snapshot -> sink(snapshot) }
}

fun <M, S, C> Env<M, C, S>.init(): Flow<Initial<S, C>> =
    flow { emit(newInitializer()) }

private fun <C> Snapshot<*, *, C>.initialCommands(): Set<C> {
    /*contract {
        returns()
    }*/
    return if (this is Initial) commands else emptySet()
}

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

fun <T> Flow<T>.startFrom(
    t: T
) = onStart { emit(t) }

/**
 * Combines given flow of states and message channel into TEA component
 */
@Deprecated("will be removed")
fun <M, S> newComponent(state: Flow<S>, messages: SendChannel<M>): ComponentLegacy<M, S> =
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

@Deprecated("will be removed")
fun <E> BroadcastChannel<E>.offerChecking(e: E) =
    check(offer(e)) { "Couldn't offer next element - $e" }

fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

@Deprecated("will be removed")
suspend fun <E> ChannelIterator<E>.nextOrNull() = if (hasNext()) next() else null

@Deprecated("will be removed")
suspend fun <E> SendChannel<E>.sendChecking(e: E) {
    check(!isClosedForSend) { "Component was already disposed" }
    send(e)
}

suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> {
    return commands.fold(LinkedHashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }
}

fun <T> Flow<T>.mergeWith(
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