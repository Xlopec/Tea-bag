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
import protocol.ComponentId
import java.util.*

sealed class PluginMessage
/*
 * UI messages
 */
sealed class UIMessage : PluginMessage()

data class UpdatePort(val port: UInt) : UIMessage()

data class UpdateHost(val host: String) : UIMessage()

object StartServer : UIMessage()

object StopServer : UIMessage()

data class RemoveSnapshots(
    val componentId: ComponentId,
    val ids: Set<UUID>
) : UIMessage()

data class RemoveAllSnapshots(
    val componentId: ComponentId
) : UIMessage()

data class ReApplyCommands(
    val componentId: ComponentId,
    val command: Value<*>
) : UIMessage()

data class ReApplyState(
    val componentId: ComponentId,
    val state: Value<*>
) : UIMessage()

data class RemoveComponent(val componentId: ComponentId) : UIMessage()
/*
 * Notifications
 */
sealed class NotificationMessage : PluginMessage()

data class NotifyOperationException(
    val exception: PluginException,
    val operation: PluginCommand? = null
) : NotificationMessage() {
    constructor(
        raw: Throwable,
        operation: PluginCommand? = null
    ) : this(raw.toPluginException(), operation)
}

data class NotifyStarted(
    val server: StartedServer
) : NotificationMessage()

data class NotifyStopped(
    val server: StoppedServer
) : NotificationMessage()

data class AppendSnapshot(
    val componentId: ComponentId,
    val message: Value<*>,
    val oldState: Value<*>,
    val newState: Value<*>
) : NotificationMessage()

data class StateReApplied(
    val componentId: ComponentId,
    val state: Value<*>
) : NotificationMessage()

data class ComponentAttached(
    val componentId: ComponentId,
    val state: Value<*>
) : NotificationMessage()