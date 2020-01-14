package com.oliynick.max.elm.time.travel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import protocol.NotifyServer

class TestDebugSession<M, S>(
    override val messages: Flow<M> = emptyFlow(),
    override val states: Flow<S> = emptyFlow()
) : DebugSession<M, S> {

    private val _sentPackets = mutableListOf<NotifyServer>()

    val sentPackets: List<NotifyServer> = _sentPackets

    override suspend fun send(packet: NotifyServer) {
        _sentPackets += packet
    }

}