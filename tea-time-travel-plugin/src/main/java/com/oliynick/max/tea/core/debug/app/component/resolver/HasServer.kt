@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.component.resolver

import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.app.transport.ServerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.withContext

interface HasServer {
    suspend fun newServer(address: ServerAddress, events: BroadcastChannel<PluginMessage>): Server
}

fun HasServer() = object : HasServer {

    override suspend fun newServer(address: ServerAddress, events: BroadcastChannel<PluginMessage>) =
        withContext(Dispatchers.IO) {
            val newServer = ServerImpl.newInstance(address, events)
            newServer.start()
            newServer
        }
}
