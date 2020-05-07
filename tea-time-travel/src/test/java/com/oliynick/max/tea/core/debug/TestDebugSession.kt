package com.oliynick.max.tea.core.debug

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.debug.protocol.NotifyServer
import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class TestDebugSession<M, S>(
    override val messages: Flow<M> = emptyFlow(),
    override val states: Flow<S> = emptyFlow()
) : DebugSession<M, S, JsonElement> {

    private val _packets = mutableListOf<NotifyServer<JsonElement>>()

    val packets: List<NotifyServer<JsonElement>> = _packets

    override suspend fun invoke(packet: NotifyServer<JsonElement>) {
        _packets += packet
    }

}