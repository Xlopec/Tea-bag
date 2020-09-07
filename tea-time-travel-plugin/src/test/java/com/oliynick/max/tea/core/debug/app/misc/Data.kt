package com.oliynick.max.tea.core.debug.app.misc

import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.transport.StartedServer
import com.oliynick.max.tea.core.debug.app.transport.StoppedServer
import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.channels.BroadcastChannel

val TestSettings = Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false)

object StoppedTestServer : StoppedServer() {

    override suspend fun start(address: ServerAddress, events: BroadcastChannel<PluginMessage>): StartedServer =
        StartedTestServer()

}

val StartedTestServerStub = object : StartedServer() {
    override suspend fun stop(): StoppedServer = StoppedTestServer
    override suspend fun invoke(component: ComponentId, message: GsonClientMessage) = Unit
}

class StartedTestServer : StartedServer() {

    private val args = mutableListOf<Pair<ComponentId, GsonClientMessage>>()

    override suspend fun stop(): StoppedServer =
        StoppedTestServer

    override suspend fun invoke(component: ComponentId, message: GsonClientMessage) {
        args += component to message
    }

}