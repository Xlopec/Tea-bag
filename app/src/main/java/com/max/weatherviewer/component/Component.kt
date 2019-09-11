@file:Suppress("unused")

// public API

package com.max.weatherviewer.component

import android.util.Log
import com.max.weatherviewer.startWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

typealias Update<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>
typealias Resolver<C, M> = suspend (command: C) -> Set<M>
typealias UpdateWith<S, C> = Pair<S, Set<C>>

class Component<M, C, S>(initialState: S,
                         private val resolver: Resolver<C, M>,
                         private val update: Update<M, S, C>) : (Flow<M>) -> Flow<S> {

    @Volatile
    private var state: S = initialState

    override fun invoke(messages: Flow<M>): Flow<S> {
        return messages.map { message -> update(message, state) }
            .flatMapConcat { (nextState, effects) ->

                effects.map { e -> invoke(resolver(e).asFlow()) }
                    .asFlow()
                    .flattenConcat()
                    .startWith(nextState)

            }
            .startWith(state)
            .onEach { s -> state = s }
            .distinctUntilChanged()
            .onEach { Log.d(this@Component.javaClass.simpleName, "State $it") }
    }

}

infix fun <S, C> S.command(command: C) = this to setOf(command)

fun <S, C> S.command(first: C, second: C) = this to setOf(first, second)

fun <S, C> S.command(first: C, second: C, third: C) = this to setOf(first, second, third)

fun <S, C> S.command(vararg commands: C) = this to setOf(*commands)

fun <S, C> S.noCommand() = this to emptySet<C>()

/**
 * Handy wrapper to perform side effect computations within coroutine scope. This function always
 * returns empty set of messages [M]
 */
suspend inline fun <M> sideEffect(crossinline action: suspend CoroutineScope.() -> Unit): Set<M> {
    return coroutineScope { action(); emptySet() }
}

/**
 * Handy wrapper to perform side effect computations within coroutine scope
 */
suspend inline fun <M> effect(crossinline action: suspend () -> M): Set<M> {
    return coroutineScope { setOf(action()) }
}