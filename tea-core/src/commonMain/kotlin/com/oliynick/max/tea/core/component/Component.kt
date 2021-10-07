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

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.internal.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
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
@UnstableApi
public typealias Sink<T> = suspend (T) -> Unit

/**
 * Creates new component using supplied values
 *
 * @param initializer initializer that provides initial values
 * @param resolver resolver that resolves messages to commands and performs side effects
 * @param updater updater that computes new states and commands to be executed
 * @param scope scope in which the sharing coroutine is started
 * @param io coroutine dispatcher which is used to execute side effects
 * @param computation coroutine dispatcher which is used to wrap [updater]'s computations
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public fun <M, C, S> Component(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    // todo: group to reduce number of arguments
    scope: CoroutineScope,
    io: CoroutineDispatcher = Dispatchers.Default,
    computation: CoroutineDispatcher = Dispatchers.Unconfined,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
): Component<M, S, C> =
    Component(Env(initializer, resolver, updater, scope, io, computation, shareOptions))

/**
 * Creates new component using preconfigured environment
 *
 * @param env preconfigured program environment
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
@OptIn(UnstableApi::class)
public fun <M, S, C> Component(
    env: Env<M, S, C>,
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)

    fun input(
        startFrom: Initial<S, C>,
    ) = env.resolveAsFlow(startFrom.commands)
        .mergeWith(input.receiveAsFlow())

    val upstream = env.upstream(env.init(), input::send, ::input)
        .shareIn(env.scope, env.shareOptions)

    return { messages -> upstream.downstream(messages, input) }
}

@UnstableApi
public fun <M, S, C> Env<M, S, C>.upstream(
    snapshots: Flow<Initial<S, C>>,
    sink: Sink<M>,
    input: (Initial<S, C>) -> Flow<M>,
): Flow<Snapshot<M, S, C>> =
    snapshots.flatMapLatest { startFrom -> compute(input(startFrom), startFrom, sink) }

@UnstableApi
public fun <M, S, C> Flow<Snapshot<M, S, C>>.downstream(
    input: Flow<M>,
    upstreamInput: SendChannel<M>,
): Flow<Snapshot<M, S, C>> =
    channelFlow {
        @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
        onStart { launch { input.into(upstreamInput) } }
            .into(channel)
    }

@UnstableApi
public fun <S, C> Env<*, S, C>.init(): Flow<Initial<S, C>> =
    channelFlow { withContext(io) { send(initializer()) } }

@UnstableApi
public fun <M, S, C> Env<M, S, C>.compute(
    input: Flow<M>,
    startFrom: Initial<S, C>,
    sink: Sink<M>,
): Flow<Snapshot<M, S, C>> {

    suspend fun newState(
        current: Snapshot<M, S, C>,
        message: M,
    ): Regular<M, S, C> {
        val (newState, commands) = update(message, current.currentState)

        return Regular(newState, commands, current.currentState, message)
    }

    return channelFlow {

        var current: Snapshot<M, S, C> = startFrom

        input
            .map { message -> newState(current, message) }
            .onEach { regular -> current = regular }
            .onEach { regular -> resolveAll(this@channelFlow, sink, regular.commands) }
            .collect(::send)

    }.startFrom(startFrom)
}

@UnstableApi
public fun <T> Flow<T>.shareIn(
    scope: CoroutineScope,
    shareOptions: ShareOptions,
): SharedFlow<T> = shareIn(scope, shareOptions.started, shareOptions.replay.toInt())

@UnstableApi
public fun <M, S, C> Env<M, S, C>.resolveAsFlow(
    commands: Collection<C>,
): Flow<M> =
    flow { emitAll(resolve(commands)) }

private fun <M, S, C> Env<M, S, C>.resolveAll(
    coroutineScope: CoroutineScope,
    sink: Sink<M>,
    commands: Iterable<C>,
) =
// launches each suspending function
// in 'launch and forget' fashion so that
// updater can process new portion of messages
    commands.forEach { command ->
        coroutineScope.launch(io + CoroutineName("Resolver coroutine: $command")) {
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
    withContext(computation) { updater(message, state) }

private suspend fun <M, C> Env<M, *, C>.resolve(
    commands: Collection<C>,
): Iterable<M> =
    commands
        .parMapTo(io, resolver::invoke)
        .flatten()
