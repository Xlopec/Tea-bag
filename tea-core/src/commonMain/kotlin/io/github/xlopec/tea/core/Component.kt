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

package io.github.xlopec.tea.core

import io.github.xlopec.tea.core.internal.mergeWith
import io.github.xlopec.tea.core.internal.startFrom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Conceptually, component is a function that accepts [flow][Flow] of messages and returns [flow][Flow]
 * of [snapshots][Snapshot] as result.
 *
 * ### Concept
 * For each incoming message component computes a pair that contain new computed state and set of
 * commands to be executed using [Updater]. Such pair or computation result is represented by the
 * [Update].
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
 * **Impure** function that performs some actions on snapshots
 * @param M message
 * @param S state
 * @param C command
 */
public typealias Interceptor<M, S, C> = suspend (snapshot: Snapshot<M, S, C>) -> Unit

/**
 * Share options used to configure the sharing coroutine
 *
 * @param started sharing strategy
 * @param replay number of states to be replayed to the downstream subscribers
 * @see [SharingStarted]
 */
public data class ShareOptions(
    val started: SharingStarted,
    val replay: UInt = 0U,
)

@ExperimentalTeaApi
public val ShareStateWhileSubscribed: ShareOptions = ShareOptions(SharingStarted.WhileSubscribed(), 1U)

/**
 * Transforms component into flow of snapshots
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
@ExperimentalTeaApi
public fun <M, S, C> Component<M, S, C>.toSnapshotsFlow(): Flow<Snapshot<M, S, C>> =
    this(emptyFlow())

/**
 * Transforms component into flow of states
 *
 * @receiver component to transform
 * @param S state
 */
@ExperimentalTeaApi
public fun <S> Component<*, S, *>.toStatesFlow(): Flow<S> =
    toSnapshotsFlow().map { snapshot -> snapshot.currentState }

/**
 * Transforms component into flow of commands
 *
 * @receiver component to transform
 * @param C command
 */
@ExperimentalTeaApi
public fun <M, S, C> Component<M, S, C>.toCommandsFlow(): Flow<Set<C>> =
    toSnapshotsFlow().map { snapshot -> snapshot.commands }

/**
 * Transforms component into function that accepts messages and returns flow that
 * emits states only
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
@ExperimentalTeaApi
public fun <M, S, C> Component<M, S, C>.toStatesComponent(): ((Flow<M>) -> Flow<S>) =
    { input -> this(input).map { snapshot -> snapshot.currentState } }

/**
 * Supplies [messages] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
@ExperimentalTeaApi
public operator fun <M, S, C> Component<M, S, C>.invoke(
    vararg messages: M,
): Flow<Snapshot<M, S, C>> = this(flowOf(*messages))

/**
 * Supplies [messages] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
@ExperimentalTeaApi
public operator fun <M, S, C> Component<M, S, C>.invoke(
    messages: Iterable<M>,
): Flow<Snapshot<M, S, C>> = this(messages.asFlow())

/**
 * Supplies [message] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
@ExperimentalTeaApi
public operator fun <M, S, C> Component<M, S, C>.invoke(
    message: M,
): Flow<Snapshot<M, S, C>> = this(flowOf(message))

/**
 * Attaches [interceptor] to the component
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
@ExperimentalTeaApi
public infix fun <M, S, C> Component<M, S, C>.with(
    interceptor: Interceptor<M, S, C>,
): Component<M, S, C> =
    { input -> this(input).onEach(interceptor) }

/**
 * Establishes subscription in ELM's terminology. It allows component
 * to listen to external messages
 *
 * @param input external messages
 * @param scope scope that will be used to manage subscription
 * @return [Job] if subscription needs to be managed manually
 */
@ExperimentalTeaApi
public fun <M> ((Flow<M>) -> Flow<*>).subscribeIn(
    input: Flow<M>,
    scope: CoroutineScope
): Job = scope.launch { invoke(input).collect() }

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

    fun messagesForInitialSnapshot(
        initial: Initial<S, C>,
    ) = toMessagesFlow(initial.commands, input.receiveAsFlow())

    val upstream = computeSnapshots(initial(), input::send, ::messagesForInitialSnapshot)
        .shareIn(scope, shareOptions)

    return { messages -> upstream.attachMessageCollector(messages, input::send) }
}

/**
 * Creates flow that for each [initial snapshot][Initial] computes flow of [snapshots][Snapshot]
 *
 * @param initials initial snapshots. Such snapshots usually come from initializer, so it means that there will be no
 * more than one initial snapshot. For each new initial snapshot new computation flow is started and the old one is
 * disposed
 * @param sink sink that consumes resolved messages
 * @param input function that will transform each [initial snapshot][Initial] into flow of messages
 */
@InternalTeaApi
public fun <M, S, C> Env<M, S, C>.computeSnapshots(
    initials: Flow<Initial<S, C>>,
    sink: Sink<M>,
    input: (Initial<S, C>) -> Flow<M>,
): Flow<Snapshot<M, S, C>> =
    initials.flatMapLatest { initial -> computeSnapshots(initial, input(initial), sink) }

/**
 * Emits snapshots emitted by receiver flow without any transformation while collecting messages input
 */
@InternalTeaApi
public fun <M, S, C> Flow<Snapshot<M, S, C>>.attachMessageCollector(
    messages: Flow<M>,
    sink: Sink<M>,
): Flow<Snapshot<M, S, C>> = channelFlow {
    launch {
        // start collector coroutine that redirects messages to sink
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
 * from [initial] snapshot
 *
 * @param initial initial snapshot state
 * @param input incoming messages for processing
 * @param sink sink that consumes resolved messages
 */
@InternalTeaApi
public fun <M, S, C> Env<M, S, C>.computeSnapshots(
    initial: Initial<S, C>,
    input: Flow<M>,
    sink: Sink<M>,
): Flow<Snapshot<M, S, C>> =
    // channel flow is for parallel decomposition
    channelFlow {
        var current: Snapshot<M, S, C> = initial
        val context = ResolveCtx(sink, this@channelFlow)

        input
            .map { message -> nextSnapshot(current, message) }
            .onEach { regular -> current = regular }
            .onEach { regular -> resolver.resolve(context, regular.commands) }
            .collect(::send)
    }.startFrom(initial)

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
): Flow<M> = channelFlow { resolver.resolve(ResolveCtx(::send, this), commands) }

/**
 * Merges messages flow produced by [initialCommands] and [messages] into single messages flow
 */
private fun <M, S, C> Env<M, S, C>.toMessagesFlow(
    initialCommands: Collection<C>,
    messages: Flow<M>,
): Flow<M> = resolveAsFlow(initialCommands).mergeWith(messages)

private fun <M, C> Resolver<C, M>.resolve(
    callContext: ResolveCtx<M>,
    commands: Iterable<C>,
) = commands.forEach { command -> this(command, callContext) }

private suspend fun <M, S, C> Env<M, S, C>.update(
    message: M,
    state: S,
): Update<S, C> =
    withContext(scope.dispatcher ?: Dispatchers.Default) { updater(message, state) }

private inline val CoroutineScope.dispatcher: CoroutineDispatcher?
    get() = coroutineContext[CoroutineDispatcher.Key]
