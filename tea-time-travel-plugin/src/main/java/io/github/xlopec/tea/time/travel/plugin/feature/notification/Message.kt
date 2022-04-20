package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.time.travel.plugin.Command
import io.github.xlopec.tea.time.travel.plugin.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.PluginException
import io.github.xlopec.tea.time.travel.plugin.domain.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotMeta
import io.github.xlopec.tea.time.travel.plugin.domain.Value
import io.github.xlopec.tea.time.travel.plugin.state.Server
import io.github.xlopec.tea.time.travel.plugin.toPluginException

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
