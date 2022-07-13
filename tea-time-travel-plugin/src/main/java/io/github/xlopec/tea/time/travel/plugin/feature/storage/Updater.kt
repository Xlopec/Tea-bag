package io.github.xlopec.tea.time.travel.plugin.feature.storage

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.StoreMessage
import io.github.xlopec.tea.time.travel.plugin.integration.onUnhandledMessage
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.componentOrThrow

internal fun State.onUpdateForStoreMessage(
    message: StoreMessage,
): Update<State, Command> = when {
    message is ExportSessions && debugger.components.isNotEmpty() -> onExportSessions(message)
    message is ImportSession -> onImportSession(message)
    else -> onUnhandledMessage(message)
}

internal fun State.onImportSession(
    message: ImportSession
): Update<State, Command> =
    this command DoImportSession(message.file)

internal fun State.onExportSessions(
    message: ExportSessions
): Update<State, Command> =
    this command DoExportSessions(message.dir, message.ids.map(debugger::componentOrThrow))
