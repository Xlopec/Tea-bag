package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.FileException
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.PluginException
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.io.File

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

// todo we should either implement ack protocol or remove this message
data class StateDeployed(
    val id: ComponentId,
    val state: Value
) : NotificationMessage

data class ComponentAttached(
    val id: ComponentId,
    val meta: SnapshotMeta,
    val state: Value,
    val commands: CollectionWrapper,
) : NotificationMessage

sealed interface ComponentImportResult : NotificationMessage

data class ComponentImportSuccess(
    val from: File,
    val sessionState: DebuggableComponent
) : ComponentImportResult

data class ComponentImportFailure(
    val exception: FileException,
) : ComponentImportResult

sealed interface ComponentExportResult : NotificationMessage

data class ComponentExportSuccess(
    val id: ComponentId,
    val file: File,
) : ComponentExportResult

data class ComponentExportFailure(
    val id: ComponentId,
    val exception: FileException,
) : ComponentExportResult
