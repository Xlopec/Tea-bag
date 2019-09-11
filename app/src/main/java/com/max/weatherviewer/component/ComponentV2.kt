@file:Suppress("unused", "MemberVisibilityCanBePrivate")

// public API

package com.max.weatherviewer.component

import com.max.weatherviewer.startWith
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class ComponentV2<M, C, S>(initialState: S,
                           private val resolver: Resolver<C, M>,
                           private val update: Update<M, S, C>) : (Flow<M>) -> Flow<S> {

    private val stateChannel = BroadcastChannel<S>(Channel.CONFLATED)
        .also { it.offer(initialState) }

    val stateChanges: Flow<S>
        get() = stateChannel.asFlow()

    override fun invoke(messages: Flow<M>): Flow<S> {
        return messages.map { message -> update(message, stateChannel.latest()) }
            .flatMapConcat { (nextState, effects) ->

                effects.map { e -> invoke(resolver(e).asFlow()) }
                    .asFlow()
                    .flattenConcat()
                    .startWith(nextState)

            }
            .onStart { emit(stateChannel.latest()) }
            .onEach { stateChannel.send(it) }
            .distinctUntilChanged()
    }

}

private suspend fun <S> BroadcastChannel<S>.latest() = openSubscription().receive()