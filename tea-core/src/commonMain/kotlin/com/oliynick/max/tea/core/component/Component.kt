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

@file:Suppress("FunctionName", "KDocUnresolvedReference")

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.internal.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.*

/**
 * Conceptually, component is a function that accepts [flow][Flow] of messages and returns [flow][Flow]
 * of [snapshots][Snapshot] as result.
 *
 * ### Concept
 * For each incoming message component computes a pair that contain new computed state and set of
 * commands to be executed using [Updater]. Such pair or computation result is represented by the
 * [UpdateWith].
 *
 * After result if obtained it's fed to [Resolver] which in turn resolves commands to messages and
 * executes side effects, if needed.
 *
 * ### Behavior notes
 * Snapshot flow sharing is controlled by the [ShareOptions], which is just a shorthand for:
 * ```
 * flow { emit(1) }.shareIn(scope, shareOptions.started, shareOptions.replay.toInt())
 * ```
 * This makes the resulting flow a hot one.
 * For more information regarding behavior and error handling see documentation for
 * [shareIn][kotlinx.coroutines.flow.shareIn] operator
 *
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public typealias Component<M, S, C> = (messages: Flow<M>) -> Flow<Snapshot<M, S, C>>

/**
 * Updater is just a regular **pure** function that accepts incoming message, state and calculates
 * a [pair][UpdateWith] that contains a new state and, possibly, empty set of commands to be resolved
 *
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public typealias Updater<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>

/**
 * Alias for kotlin's [Pair]. It can be created using the following [extensions][command]
 *
 * @param S state of the component
 * @param C commands to be executed. There's **NO GUARANTEE** of commands ordering, they can be
 * executed in any order. That implies calculation correctness mustn't depend on the ordering
 *
 * @param S state of the application
 * @param C commands to be executed
 */
public typealias UpdateWith<S, C> = Pair<S, Set<C>>

/**
 * Alias for a possibly **impure** function that resolves commands to messages and performs side
 * effects.
 *
 * ### Exceptions
 *
 * Any exception that happens inside this function will redelivered to a [Component]'s scope and handled
 * by it. For more information regarding error handling see [shareIn][kotlinx.coroutines.flow.shareIn]
 *
 * @param M incoming messages
 * @param C commands to be executed
 */
public typealias Resolver<C, M> = suspend (command: C) -> Set<M>

/**
 * Type alias for suspending function that accepts incoming values a puts it to a queue for later
 * processing
 *
 * @param T incoming values
 */
@InternalTeaApi
public typealias Sink<T> = suspend (T) -> Unit

/**
 * Creates new component using supplied values
 *
 * @param initializer initializer that provides initial values
 * @param resolver resolver that resolves messages to commands and performs side effects
 * @param updater updater that computes new states and commands to be executed
 * @param scope scope in which the sharing coroutine is started
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public fun <M, C, S> Component(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    scope: CoroutineScope,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
): Component<M, S, C> =
    Component(Env(initializer, resolver, updater, scope, shareOptions))

/**
 * Creates new component using preconfigured environment
 *
 * @param env preconfigured program environment
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public fun <M, S, C> Component(
    env: Env<M, S, C>,
): Component<M, S, C> = with(env) {

    val input = Channel<M>(RENDEZVOUS)

    fun messagesForSnapshot(
        startFrom: Initial<S, C>,
    ) = toMessagesFlow(startFrom.commands, input.receiveAsFlow())

    val componentFlow = toComponentFlow(initial(), input::send, ::messagesForSnapshot)
        .shareIn(scope, shareOptions)

    return { messages -> componentFlow.withMessageCollector(messages, input::send) }
}

/**
 * Creates flow that for each [initial snapshot][Initial] computes flow of [snapshots][Snapshot]
 *
 * @param initialSnapshots initial snapshots. Such snapshots usually come from initializer, so it means that there will be no
 * more than one initial snapshot. For each new initial snapshot new computation flow is started and the old one is
 * disposed
 * @param sink sink that consumes resolved messages
 * @param input function that will transform each [initial snapshot][Initial] into flow of messages
 */
@InternalTeaApi
public fun <M, S, C> Env<M, S, C>.toComponentFlow(
    initialSnapshots: Flow<Initial<S, C>>,
    sink: Sink<M>,
    input: (Initial<S, C>) -> Flow<M>,
): Flow<Snapshot<M, S, C>> =
    initialSnapshots.flatMapLatest { startFrom -> toComputationFlow(input(startFrom), startFrom, sink) }

/**
 * Emits snapshots emitted by receiver flow without any transformation
 * while collecting messages input
 */
@InternalTeaApi
public fun <M, S, C> Flow<Snapshot<M, S, C>>.withMessageCollector(
    messages: Flow<M>,
    sink: Sink<M>,
): Flow<Snapshot<M, S, C>> = channelFlow {
    launch {
        // redirects input to sink
        messages.collect(sink::invoke)
    }
    collect(::send)
}

/**
 * Calculates initial snapshot by invoking initializer inside app environment
 */
@InternalTeaApi
public fun <S, C> Env<*, S, C>.initial(): Flow<Initial<S, C>> =
    // channel flow is for parallel decomposition
    channelFlow { send(initializer()) }

/**
 * For each new message from [input] calculates next [application state, message and commands][Regular].
 * This function resolves messages from commands. Sink is fed with resolved messages. Flow emission starts
 * from [startFrom] snapshot
 */
@InternalTeaApi
public fun <M, S, C> Env<M, S, C>.toComputationFlow(
    input: Flow<M>,
    startFrom: Initial<S, C>,
    sink: Sink<M>,
): Flow<Snapshot<M, S, C>> =
    // channel flow is for parallel decomposition
    channelFlow {
        var current: Snapshot<M, S, C> = startFrom

        input
            .map { message -> nextSnapshot(current, message) }
            .onEach { regular -> current = regular }
            .onEach { regular -> resolveAll(this@channelFlow, sink, regular.commands) }
            .collect(::send)

    }.startFrom(startFrom)

/**
 * Calculates next snapshot for given arguments on [Env.scope] dispatcher
 */
@InternalTeaApi
public suspend fun <M, S, C> Env<M, S, C>.nextSnapshot(
    current: Snapshot<M, S, C>,
    message: M,
): Regular<M, S, C> {
    val (newState, commands) = update(message, current.currentState)

    return Regular(newState, commands, current.currentState, message)
}

/**
 * Shorthand for
 *
 * ```kotlin
 * flow.shareIn(scope, shareOptions.started, shareOptions.replay.toInt())
 * ```
 */
@InternalTeaApi
public fun <T> Flow<T>.shareIn(
    scope: CoroutineScope,
    shareOptions: ShareOptions,
): SharedFlow<T> = shareIn(scope, shareOptions.started, shareOptions.replay.toInt())

/**
 * Resolves [commands] to message flow
 */
@InternalTeaApi
public fun <M, S, C> Env<M, S, C>.resolveAsFlow(
    commands: Iterable<C>,
): Flow<M> =
    flow { emitAll(resolve(commands)) }

/**
 * Merges messages flow produced by [initialCommands] and [messages] into single messages flow
 */
private fun <M, S, C> Env<M, S, C>.toMessagesFlow(
    initialCommands: Collection<C>,
    messages: Flow<M>,
): Flow<M> = resolveAsFlow(initialCommands).mergeWith(messages)

private fun <M, S, C> Env<M, S, C>.resolveAll(
    coroutineScope: CoroutineScope,
    sink: Sink<M>,
    commands: Iterable<C>,
) =
// launches each suspending function
// in 'launch and forget' fashion so that
// updater can process new portion of messages
    commands.forEach { command ->
        coroutineScope.launch(CoroutineName("Resolver coroutine: $command")) {
            sink(resolver(command))
        }
    }

private suspend operator fun <E> Sink<E>.invoke(
    elements: Iterable<E>,
) = elements.forEach { e -> invoke(e) }

private suspend fun <M, S, C> Env<M, S, C>.update(
    message: M,
    state: S,
): UpdateWith<S, C> =
    withContext(scope.dispatcher ?: Dispatchers.Default) { updater(message, state) }

private suspend fun <M, C> Env<M, *, C>.resolve(
    commands: Iterable<C>,
): Iterable<M> =
    commands
        .parMapTo(Unconfined, resolver::invoke)
        .flatten()

private inline val CoroutineScope.dispatcher: CoroutineDispatcher?
    get() = coroutineContext[CoroutineDispatcher.Key]