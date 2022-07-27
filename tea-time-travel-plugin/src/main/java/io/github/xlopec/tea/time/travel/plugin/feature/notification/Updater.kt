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

package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStopServer
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.integration.*
import io.github.xlopec.tea.time.travel.plugin.model.*
import java.util.*

internal fun State.updateForNotificationMessage(
    message: NotificationMessage,
): Update<State, Command> =
    when (message) {
        is ServerStarted -> onStarted(message.server)
        is ServerStopped -> onStopped()
        is AppendSnapshot -> onAppendSnapshot(message)
        is StateDeployed -> onStateDeployed(message)
        is ComponentAttached -> onComponentAttached(message)
        is ComponentImportResult -> onComponentImportResult(message)
        is ComponentExportResult -> onComponentExportResult(message)
        is OperationException -> onOperationException(message)
        else -> onUnhandledMessage(message)
    }

private fun State.onComponentImportResult(
    message: ComponentImportResult
) = when (message) {
    is ComponentImportFailure -> onComponentImportFailure(message)
    is ComponentImportSuccess -> onComponentImportSuccess(message)
}

private fun State.onComponentImportFailure(
    message: ComponentImportFailure
) = command(
    DoNotifyFileOperationFailure(
        title = "Import failure",
        description = formatExceptionDescription("Couldn't import session", message.exception, ". Check if file is valid"),
        forFile = message.exception.forFile
    )
)

// overwrite any existing session for now
// todo: show prompt dialog in future with options to merge, overwrite and cancel
private fun State.onComponentImportSuccess(
    message: ComponentImportSuccess,
): Update<State, Command> =
    debugger(debugger.attachComponent(message.sessionState.id, message.sessionState)) command DoNotifyFileOperationSuccess(
        title = "Import success",
        description = "Session \"${message.sessionState.id.value}\" were imported",
        forFile = message.from,
    )

private fun State.onComponentExportResult(
    message: ComponentExportResult
) = when (message) {
    is ComponentExportFailure -> onExportFailure(message)
    is ComponentExportSuccess -> onExportSuccess(message)
}

private fun State.onExportSuccess(
    message: ComponentExportSuccess
) = command(
    DoNotifyFileOperationSuccess(
        title = "Export success",
        description = "Session \"${message.id.value}\" were exported",
        forFile = message.file,
    )
)

private fun State.onExportFailure(
    message: ComponentExportFailure
) = command(
    DoNotifyFileOperationFailure(
        title = "Export failure",
        description = formatExceptionDescription("Failed to export \"${message.id.value}\"", message.exception),
        forFile = message.exception.forFile
    ),
)

private fun State.onStarted(
    server: Server,
) = copy(server = server).noCommand()

private fun State.onStopped() = copy(server = null).noCommand()

private fun State.onAppendSnapshot(
    message: AppendSnapshot,
): Update<State, Command> = debugger(
    debugger.appendSnapshot(
        id = message.componentId,
        snapshot = message.toSnapshot(),
        newState = message.newState,
        maxRetainedSnapshots = settings.maxRetainedSnapshots
    )
).noCommand()

private fun State.onComponentAttached(
    message: ComponentAttached,
): Update<State, Command> =
    debugger(
        debugger.attachComponent(
            id = message.id,
            state = message.state,
            snapshot = message.toSnapshot(),
            clearSnapshotsOnAttach = settings.clearSnapshotsOnAttach
        )
    ) command DoNotifyComponentAttached(message.id)

private fun State.onStateDeployed(
    message: StateDeployed,
): Update<State, Command> {
    val component = debugger.components[message.id] ?: return noCommand()
    val updated = component.copy(state = message.state)

    return debugger(debugger.updateComponent(updated.id, updated)).noCommand()
}

private fun State.onOperationException(
    message: OperationException,
): Update<State, Command> =
    when {
        isFatalProblem(message.exception, message.operation) -> notifyDeveloperException(message.exception)
        isManipulateServerException(message) -> onManipulateServerException(message)
        else -> onNonFatalOperationException(message)
    }

private fun isManipulateServerException(
    message: OperationException
) = message.operation is DoStartServer || message.operation is DoStopServer

private fun State.onManipulateServerException(
    message: OperationException
) = copy(server = null) command DoNotifyOperationException(message)

private fun State.onNonFatalOperationException(
    message: OperationException
) = this command DoNotifyOperationException(message)

private fun isFatalProblem(
    th: PluginException,
    op: Command?,
): Boolean =
    op is DoStopServer || op is DoStoreSettings || th is InternalException

private fun AppendSnapshot.toSnapshot() = OriginalSnapshot(meta, message, newState, commands)

private fun ComponentAttached.toSnapshot() = OriginalSnapshot(meta, null, state, commands)

private fun DoNotifyOperationException(
    message: OperationException,
) = DoNotifyOperationException(message.exception, message.operation, message.description)

private fun notifyDeveloperException(cause: Throwable): Nothing =
    throw IllegalStateException(
        "Unexpected exception. Please, inform developers about the problem",
        cause
    )

internal fun formatExceptionDescription(
    prefix: String,
    exception: PluginException,
    suffix: String = "",
) = "$prefix${exception.message?.toSanitizedString()?.let { ", caused by \"$it\"" } ?: ""}$suffix"

private fun String.toSanitizedString() = replaceFirstChar { it.lowercase(Locale.ROOT) }
