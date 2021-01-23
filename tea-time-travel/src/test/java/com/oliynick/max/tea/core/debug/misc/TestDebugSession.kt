package com.oliynick.max.tea.core.debug.misc

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.debug.gson.GsonNotifyServer
import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class TestDebugSession<M, S>(
    override val messages: Flow<M> = emptyFlow(),
    override val states: Flow<S> = emptyFlow()
) : DebugSession<M, S, JsonElement> {

    private val _packets = mutableListOf<GsonNotifyServer>()

    val packets: List<GsonNotifyServer> = _packets

    override suspend fun invoke(packet: GsonNotifyServer) {
        _packets += packet
    }

}