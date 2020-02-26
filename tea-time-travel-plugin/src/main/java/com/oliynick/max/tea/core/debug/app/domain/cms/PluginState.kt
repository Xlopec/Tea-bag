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

package com.oliynick.max.tea.core.debug.app.domain.cms

import com.oliynick.max.tea.core.debug.app.transport.StartedServer
import com.oliynick.max.tea.core.debug.app.transport.StoppedServer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import protocol.ComponentId
import java.time.LocalDateTime
import java.util.*

typealias ComponentMapping = PersistentMap<ComponentId, ComponentDebugState>

const val defaultHost = "0.0.0.0"
const val defaultPort = 8080U

//todo add remote call timeout
data class ServerSettings(
    val host: String,
    val port: UInt
)

data class Settings(
    val serverSettings: ServerSettings
)

data class DebugState(
    val components: ComponentMapping = persistentMapOf()
)

data class Snapshot(
    val id: UUID,
    val timestamp: LocalDateTime,
    val message: Value<*>,
    val state: Value<*>
)

data class ComponentDebugState(
    val id: ComponentId,
    val currentState: Value<*>,
    val snapshots: PersistentList<Snapshot> = persistentListOf()
)

sealed class PluginState {
    abstract val settings: Settings
}

data class Stopped(
    override val settings: Settings,
    val server: StoppedServer
) : PluginState()

data class Starting(
    override val settings: Settings
) : PluginState()

data class Started(
    override val settings: Settings,
    val debugState: DebugState,
    val server: StartedServer
) : PluginState()

data class Stopping(
    override val settings: Settings
) : PluginState()