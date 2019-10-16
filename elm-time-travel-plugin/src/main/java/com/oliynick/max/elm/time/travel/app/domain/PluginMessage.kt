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
/*
 * UI messages
 */
sealed class UIMessage : PluginMessage()

data class AddFiles(val files: List<File>) : UIMessage()

data class RemoveFiles(val files: List<File>) : UIMessage()

data class UpdatePort(val port: UInt) : UIMessage()

data class UpdateHost(val host: String) : UIMessage()

object StartServer : UIMessage()

object StopServer : UIMessage()

data class RemoveSnapshots(val componentId: ComponentId, val ids: Set<UUID>) : UIMessage()

data class ReApplyCommands(val componentId: ComponentId, val commands: List<Any>) : UIMessage() {
    constructor(componentId: ComponentId, command: Any) : this(componentId, listOf(command))
}

data class ReApplyState(val componentId: ComponentId, val state: Any) : UIMessage()

data class RemoveComponent(val componentId: ComponentId) : UIMessage()
/*
 * Notifications
 */
sealed class NotificationMessage : PluginMessage()

data class NotifyMissingDependency(val exception: ClassNotFoundException) : NotificationMessage()

data class NotifyOperationException(val exception: Throwable) : NotificationMessage()

object NotifyStarted : NotificationMessage()

object NotifyStopped : NotificationMessage()

data class AppendSnapshot(val componentId: ComponentId, val message: Any, val oldState: Any, val newState: Any) : NotificationMessage()

data class StateReApplied(val componentId: ComponentId, val state: Any) : NotificationMessage()

data class ComponentAttached(val componentId: ComponentId, val state: Any) : NotificationMessage()