package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.PluginException
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId

data class OperationException(
    val exception: PluginException,
    val operation: Command? = null,
    val description: String? = null,
) : NotificationMessage

@JvmInline
value class ServerStarted(
    val server: Server
) : NotificationMessage

object ServerStopped : NotificationMessage

data class AppendSnapshot(
    val componentId: ComponentId,
    val meta: SnapshotMeta,
    val message: Value,
    val oldState: Value,
    val newState: Value,
    val commands: CollectionWrapper,
) : NotificationMessage

data class StateDeployed(
    val componentId: ComponentId,
    val state: Value
) : NotificationMessage

data class ComponentAttached(
    val id: ComponentId,
    val meta: SnapshotMeta,
    val state: Value,
    val commands: CollectionWrapper,
) : NotificationMessage

@JvmInline
value class ComponentImported(
    val sessionState: DebuggableComponent
) : NotificationMessage
