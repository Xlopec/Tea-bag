package com.max.weatherviewer.component

import android.util.Log
import com.max.weatherviewer.startWith
import kotlinx.coroutines.flow.*

typealias Update<M, S, C> = (message: M, state: S) -> Pair<S, C?>
typealias Resolver<C, M> = suspend (command: C) -> M?

class Component<M, C, S>(initialState: S,
                         val resolver: Resolver<C, M>,
                         val update: Update<M, S, C>) : (Flow<M>) -> Flow<S> {

    @Volatile
    private var state: S = initialState

    override fun invoke(messages: Flow<M>): Flow<S> {
        return messages.map { message -> update(message, state) }
            .flatMapConcat { (nextState, effect) ->
                flow { emitAll(invoke(flowOf(resolver(effect ?: return@flow) ?: return@flow))) }
                    .startWith(nextState)
            }
            .startWith(state)
            .onEach { s -> state = s }
            .distinctUntilChanged()
            .onEach { Log.d(this@Component.javaClass.simpleName, "State $it") }
    }

}