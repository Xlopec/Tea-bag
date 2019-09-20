/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.oliynick.max.elm.core.component

typealias Persist<M, S> = suspend (M, S) -> Unit
typealias PersistedData<M, S> = Pair<M, S>

/*suspend fun <M, C, S> load(loader: Loader<M, S>, ifNone: S, resolver: Resolver<C, M>, update: Update<M, S, C>): Component<M, S> {

    val stored = loader()

    if (stored == null) {
        return component(ifNone, resolver, update)
    }

    val (message, state) = stored
    //val (nextState, commands) = update(message, state)

    val c: (Flow<M>) -> Flow<S> = component(state, resolver, update).also {
        it(flowOf(message))
    }
}*/

/*
private class PersistComponent<M, C, S>(private val loader: Loader<M, S>,
                                        ifNone: S,
                                        resolver: Resolver<C, M>, update: Update<M, S, C>) : (Flow<M>) -> Flow<S> {

    private val delegate: (Flow<M>) -> Flow<S>

    init {
        //GlobalScope.launch {
            val stored = loader()

            if (stored != null) {
                val (message, state) = stored

                delegate = component(state, resolver, update)

                GlobalScope.launch { delegate(flowOf(message)) }

            } else {
                delegate = component(ifNone, resolver, update)
            }
       // }
    }

    override fun invoke(messages: Flow<M>): Flow<S> = TODO()//delegate(messages.onEach(messageLog)).onEach(stateLog)

}*/
