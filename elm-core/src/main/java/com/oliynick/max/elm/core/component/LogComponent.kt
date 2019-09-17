package com.oliynick.max.elm.core.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

typealias Logger<F> = suspend (F) -> Unit

class LogComponent<M, S>(private val stateLog: Logger<S>,
                         private val messageLog: Logger<M>,
                         private val delegate: (Flow<M>) -> Flow<S>) : (Flow<M>) -> Flow<S> {

    override fun invoke(messages: Flow<M>): Flow<S> {
        return delegate(messages.onEach(messageLog)).onEach(stateLog)
    }

}