package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.time.travel.session.DebugSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import protocol.NotifyServer

class TestDebugSession<M, S>(
    override val messages: Flow<M> = emptyFlow(),
    override val states: Flow<S> = emptyFlow()
) : DebugSession<M, S> {

    private val _packets = mutableListOf<NotifyServer>()

    val packets: List<NotifyServer> = _packets

    override suspend fun send(packet: NotifyServer) {
        _packets += packet
    }

}