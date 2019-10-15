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

package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.io.File
import java.util.*

sealed class PluginMessage

data class AddFiles(val files: List<File>) : PluginMessage()

data class RemoveFiles(val files: List<File>) : PluginMessage()

data class UpdatePort(val port: UInt) : PluginMessage()

data class UpdateHost(val host: String) : PluginMessage()

data class NotifyMissingDependency(val exception: ClassNotFoundException) : PluginMessage()

data class NotifyOperationException(val exception: Throwable) : PluginMessage()

object NotifyStarted : PluginMessage()

object NotifyStopped : PluginMessage()

object StartServer : PluginMessage()

object StopServer : PluginMessage()

data class AppendSnapshot(val componentId: ComponentId, val message: Any, val oldState: Any, val newState: Any) : PluginMessage()

data class RemoveSnapshots(val componentId: ComponentId, val ids: Set<UUID>) : PluginMessage()

data class ReApplyCommands(val componentId: ComponentId, val commands: List<Any>) : PluginMessage()

data class ReApplyState(val componentId: ComponentId, val state: Any) : PluginMessage()

data class RemoveComponent(val componentId: ComponentId) : PluginMessage()

data class StateReApplied(val componentId: ComponentId, val state: Any) : PluginMessage()

data class ComponentAttached(val componentId: ComponentId, val state: Any) : PluginMessage()