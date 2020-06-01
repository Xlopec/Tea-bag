/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.transport

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.protocol.ClientMessage
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.withContext

sealed class ServerResource

abstract class StoppedServer : ServerResource() {

    abstract suspend fun start(
        address: ServerAddress,
        events: BroadcastChannel<PluginMessage>
    ): StartedServer
}

abstract class StartedServer : ServerResource() {

    abstract suspend fun stop(): StoppedServer

    abstract suspend operator fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    )

}

fun NewStoppedServer(): StoppedServer = StoppedServerImpl()

private class StoppedServerImpl : StoppedServer() {

    override suspend fun start(address: ServerAddress, events: BroadcastChannel<PluginMessage>): StartedServer =
        withContext(Dispatchers.IO) {
            val newServer = Server.newInstance(address, events)
            newServer.start()
            StartedServerImpl(newServer)
        }

}

private class StartedServerImpl(
    private val server: Server
) : StartedServer() {

    override suspend fun stop(): StoppedServer =
        withContext(Dispatchers.IO) {
            server.stop(1, 1)
            StoppedServerImpl()
        }

    override suspend fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    ) = server(component, message)

}
