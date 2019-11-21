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

package com.oliynick.max.elm.time.travel.app.transport

import com.oliynick.max.elm.time.travel.app.domain.PluginMessage
import com.oliynick.max.elm.time.travel.app.domain.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import protocol.ClientMessage
import protocol.ComponentId
import java.util.concurrent.TimeUnit

// fixme shiii
class ServerHandler {
    private var server: Server? = null
    private var collectJob: Job? = null
    private val mutex = Mutex()

    suspend fun start(settings: Settings, events: Channel<PluginMessage>) {
        mutex.withLock {

            require(server == null) { "server haven't been disposed" }

            val newServer = Server.newInstance(settings, events)

            server = newServer

            withContext(Dispatchers.IO) {
                newServer.start()
            }
        }
    }

    suspend operator fun invoke(component: ComponentId, message: ClientMessage) {
        mutex.withLock {
            server!!(component, message)
        }
    }

    suspend fun stop() {
        // todo some kind of readLock
        mutex.withLock {
            val old = server

            requireNotNull(old) { "server haven't been started" }

            server = null

            withContext(Dispatchers.IO) {
                collectJob?.cancel()
                old.stop(1, 1, TimeUnit.SECONDS)
            }
        }
    }
}