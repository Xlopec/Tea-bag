/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

package com.oliynick.max.tea.core.debug.app.component.cms

import com.oliynick.max.tea.core.debug.app.component.cms.message.Message
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.app.transport.ServerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.withContext

interface HasServer {
    suspend fun newServer(address: ServerAddress, events: BroadcastChannel<Message>): Server
}

fun HasServer() = object : HasServer {

    override suspend fun newServer(address: ServerAddress, events: BroadcastChannel<Message>) =
        withContext(Dispatchers.IO) {
            val newServer = ServerImpl.newInstance(address, events)
            newServer.start()
            newServer
        }
}
