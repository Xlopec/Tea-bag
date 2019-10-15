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
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import com.oliynick.max.elm.time.travel.protocol.Message
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class EngineManager(
    private val serverProvider: (
        settings: Settings,
        events: Channel<PluginMessage>,
        outgoing: Channel<Pair<ComponentId, Message>>
    ) -> ApplicationEngine
) {
    private var engine: ApplicationEngine? = null

    val outgoing = Channel<Pair<ComponentId, Message>>()

    suspend fun start(settings: Settings, events: Channel<PluginMessage>) {
        withContext(Dispatchers.IO) {
            require(engine == null) { "server haven't been disposed" }

            serverProvider(settings, events, outgoing).also { newEngine ->
                engine = newEngine
                newEngine.start()
            }
        }
    }

    suspend fun stop() {
        val old = engine
        engine = null

        requireNotNull(old) { "server haven't been started" }

        withContext(Dispatchers.IO) {
            old.stop(1, 1, TimeUnit.SECONDS)
        }
    }
}