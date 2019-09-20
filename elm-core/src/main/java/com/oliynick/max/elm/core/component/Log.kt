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

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

typealias Logger<F> = suspend (F) -> Unit

inline fun <M, S> ((Flow<M>) -> Flow<S>).withAndroidLogger(tag: String = this::class.simpleName ?: toString(),
                                                           crossinline sTransform: (S) -> String = { "State $it" },
                                                           crossinline mTransform: (M) -> String = { "Message $it" }): (Flow<M>) -> Flow<S> {

    return withLogger({ s -> Log.d(tag, sTransform(s)) }, { m -> Log.d(tag, mTransform(m)) })
}

fun <M, S> ((Flow<M>) -> Flow<S>).withLogger(stateLog: Logger<S>, messageLog: Logger<M>): (Flow<M>) -> Flow<S> {
    return LogComponent(stateLog, messageLog, this)
}

private class LogComponent<M, S>(private val stateLog: Logger<S>,
                                 private val messageLog: Logger<M>,
                                 private val delegate: (Flow<M>) -> Flow<S>) : (Flow<M>) -> Flow<S> {

    override fun invoke(messages: Flow<M>): Flow<S> = delegate(messages.onEach(messageLog)).onEach(stateLog)

}