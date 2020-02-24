package com.oliynick.max.tea.core.debug

import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import protocol.NotifyServer

class TestDebugSession<M, S>(
    override val messages: Flow<M> = emptyFlow(),
    override val states: Flow<S> = emptyFlow()
) : DebugSession<M, S> {

    private val _packets = mutableListOf<NotifyServer>()

    val packets: List<NotifyServer> = _packets

    override suspend fun invoke(packet: NotifyServer) {
        _packets += packet
    }

}