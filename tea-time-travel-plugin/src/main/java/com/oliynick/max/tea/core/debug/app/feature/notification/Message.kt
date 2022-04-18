package com.oliynick.max.tea.core.debug.app.feature.notification

import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.NotificationMessage
import com.oliynick.max.tea.core.debug.app.PluginException
import com.oliynick.max.tea.core.debug.app.domain.CollectionWrapper
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.SnapshotMeta
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.state.Server
import com.oliynick.max.tea.core.debug.app.toPluginException
import io.github.xlopec.tea.core.debug.protocol.ComponentId

data class OperationException(
    val exception: PluginException,
    val operation: Command? = null,
    val description: String? = null,
) : NotificationMessage {
    constructor(
        raw: Throwable,
        operation: Command? = null,
        description: String? = null
    ) : this(raw.toPluginException(), operation, description)
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

@JvmInline
value class ComponentImported(
    val sessionState: ComponentDebugState
) : NotificationMessage
