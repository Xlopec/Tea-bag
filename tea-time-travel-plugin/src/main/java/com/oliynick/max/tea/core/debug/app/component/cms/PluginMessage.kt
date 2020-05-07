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

package com.oliynick.max.tea.core.debug.app.component.cms

import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.transport.StartedServer
import com.oliynick.max.tea.core.debug.app.transport.StoppedServer
import com.oliynick.max.tea.core.debug.protocol.ComponentId

sealed class PluginMessage

/*
 * UI messages
 */
sealed class UIMessage : PluginMessage()

data class UpdateDebugSettings(
    val isDetailedToStringEnabled: Boolean
) : UIMessage()

data class UpdatePort(
    val port: UInt
) : UIMessage()

data class UpdateHost(
    val host: String
) : UIMessage()

data class UpdateFilter(
    val id: ComponentId,
    val input: String,
    val ignoreCase: Boolean,
    val option: FilterOption
) : UIMessage()

object StartServer : UIMessage()

object StopServer : UIMessage()

data class RemoveSnapshots(
    val componentId: ComponentId,
    val ids: Set<SnapshotId>
) : UIMessage() {

    constructor(
        componentId: ComponentId,
        id: SnapshotId
    ) : this(componentId, setOf(id))

}

data class RemoveAllSnapshots(
    val componentId: ComponentId
) : UIMessage()

data class ReApplyMessage(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : UIMessage()

data class ReApplyState(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : UIMessage()

data class RemoveComponent(
    val componentId: ComponentId
) : UIMessage()

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
    val message: Value,
    val oldState: Value,
    val newState: Value
) : NotificationMessage()

data class StateReApplied(
    val componentId: ComponentId,
    val state: Value
) : NotificationMessage()

data class ComponentAttached(
    val componentId: ComponentId,
    val state: Value
) : NotificationMessage()