package com.oliynick.max.tea.core.debug.app.message

import com.oliynick.max.tea.core.debug.app.command.Command
import com.oliynick.max.tea.core.debug.app.domain.CollectionWrapper
import com.oliynick.max.tea.core.debug.app.domain.SnapshotMeta
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.resolve.PluginException
import com.oliynick.max.tea.core.debug.app.resolve.toPluginException
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

/*
 * Notifications
 */
sealed interface NotificationMessage : Message

data class NotifyOperationException(
    val exception: PluginException,
    val operation: Command? = null
) : NotificationMessage {
    constructor(
        raw: Throwable,
        operation: Command? = null
    ) : this(raw.toPluginException(), operation)
}

@JvmInline
value class NotifyStarted(
    val server: Server
) : NotificationMessage

object NotifyStopped : NotificationMessage

data class AppendSnapshot(
    val componentId: ComponentId,
    val meta: SnapshotMeta,
    val message: Value,
    val oldState: Value,
    val newState: Value,
    val commands: CollectionWrapper,
) : NotificationMessage

data class StateApplied(
    val componentId: ComponentId,
    val state: Value
) : NotificationMessage

data class ComponentAttached(
    val componentId: ComponentId,
    val meta: SnapshotMeta,
    val state: Value,
    val commands: CollectionWrapper,
) : NotificationMessage
