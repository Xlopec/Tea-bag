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

package com.oliynick.max.tea.core.debug.app.component.cms

import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

sealed class PluginMessage

/*
 * UI messages
 */
sealed class UIMessage : PluginMessage()

data class UpdateDebugSettings(
    val isDetailedToStringEnabled: Boolean
) : UIMessage()

data class UpdateServerSettings(
    val host: String,
    val port: String
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

data class ApplyMessage(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : UIMessage()

data class ApplyState(
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
    val server: Server
) : NotificationMessage()

object NotifyStopped : NotificationMessage()

data class AppendSnapshot(
    val componentId: ComponentId,
    val meta: SnapshotMeta,
    val message: Value,
    val oldState: Value,
    val newState: Value
) : NotificationMessage()

data class StateApplied(
    val componentId: ComponentId,
    val state: Value
) : NotificationMessage()

data class ComponentAttached(
    val componentId: ComponentId,
    val state: Value
) : NotificationMessage()
