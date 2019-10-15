package com.oliynick.max.elm.core.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Internal alias of a component
 */
internal typealias ComponentInternal<M, S> = Pair<SendChannel<M>, Flow<S>>

/**
 * Dependencies holder
 */
internal data class Dependencies<M, C, S>(inline val initializer: Initializer<S, C>,
                                          inline val resolver: Resolver<C, M>,
                                          inline val update: Update<M, S, C>,
                                          inline val interceptor: Interceptor<M, S, C>)

internal fun <M, C, S> CoroutineScope.actorComponent(initializer: Initializer<S, C>,
                                                     resolver: Resolver<C, M>,
                                                     update: Update<M, S, C>,
                                                     interceptor: Interceptor<M, S, C>): ComponentInternal<M, S> {

    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    @UseExperimental(ObsoleteCoroutinesApi::class)
    return this@actorComponent.actor<M>(coroutineContext.jobOrDefault(),
                                        onCompletion = statesChannel::close) {

        loop(initializer, Dependencies(initializer, resolver, update, interceptor), channel, statesChannel)

    } to statesChannel.asFlow()
}

/**
 * Stores a new state to channel and notifies subscribers about changes
 */
internal suspend fun <M, C, S> updateMutating(message: M,
                                              state: S,
                                              dependencies: Dependencies<M, C, S>,
                                              states: BroadcastChannel<S>): UpdateWith<S, C> {

    return dependencies.update(message, state)
        // we don't want to suspend here
        .also { (nextState, _) -> states.offerChecking(nextState) }
        .also { (nextState, commands) -> dependencies.interceptor(message, state, nextState, commands) }
}

/**
 * Polls messages from channel's iterator and computes subsequent component's states.
 * Before polling a message from the channel it tries to computes all
 * subsequent states produced by resolved commands
 */
internal tailrec suspend fun <M, C, S> loop(state: S,
                                            it: ChannelIterator<M>,
                                            dependencies: Dependencies<M, C, S>,
                                            states: BroadcastChannel<S>): S {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = updateMutating(message, state, dependencies, states)

    return loop(loop(nextState, dependencies.resolver(commands).iterator(), dependencies, states), it, dependencies, states)
}

/**
 * Polls messages from collection's iterator and computes next states until collection is empty
 */
internal suspend fun <M, C, S> loop(state: S,
                                    it: Iterator<M>,
                                    dependencies: Dependencies<M, C, S>,
                                    states: BroadcastChannel<S>): S {

    val message = it.nextOrNull() ?: return state

    val (nextState, commands) = updateMutating(message, state, dependencies, states)

    return loop(loop(nextState, it, dependencies, states), dependencies.resolver(commands).iterator(), dependencies, states)
}

/**
 * Loads an initial state using supplied initializer and starts component's loop
 */
internal suspend fun <M, C, S> loop(initializer: Initializer<S, C>,
                                    dependencies: Dependencies<M, C, S>,
                                    messages: Channel<M>,
                                    states: BroadcastChannel<S>): S {

    val (initState, initCommands) = initializer()
        .also { (initialState, _) -> states.offerChecking(initialState) }

    val nonTransient = loop(initState, dependencies.resolver(initCommands).iterator(), dependencies, states)

    return loop(nonTransient, messages.iterator(), dependencies, states)
}

/**
 * Combines given flow of states and message channel into TEA component
 */
internal fun <M, S> newComponent(state: Flow<S>, messages: SendChannel<M>): Component<M, S> {
    return { input ->

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
}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
internal suspend fun emptyInterceptor(message: Any, prevState: Any, newState: Any, commands: Set<*>) = Unit

internal fun <E> BroadcastChannel<E>.offerChecking(e: E) = check(offer(e)) { "Couldn't offer next element - $e" }

internal fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

internal suspend fun <E> ChannelIterator<E>.nextOrNull() = if (hasNext()) next() else null

internal suspend fun <E> SendChannel<E>.sendChecking(e: E) {
    check(!isClosedForSend) { "Component was already disposed" }
    send(e)
}

internal suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> {
    return commands.fold(HashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }
}

internal fun CoroutineContext.jobOrDefault(): Job = this[Job] ?: Job()

internal val Unit?.safe get() = this