package com.oliynick.max.tea.core.debug.session

import kotlinx.coroutines.flow.Flow
import protocol.NotifyServer

interface DebugSession<M, S> {

    val messages: Flow<M>
    val states: Flow<S>

    suspend fun send(
        packet: NotifyServer
    )

}