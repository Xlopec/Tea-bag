package io.github.xlopec.tea.time.travel.plugin.feature.storage

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.StoreMessage
import io.github.xlopec.tea.time.travel.plugin.integration.warnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.component

internal fun updateForStoreMessage(
    message: StoreMessage,
    state: State,
): Update<State, Command> = when {
    message is ExportSessions && state.debugger.components.isNotEmpty() -> exportSessions(message, state)
    message is ImportSession -> importSession(message, state)
    else -> warnUnacceptableMessage(message, state)
}

internal fun importSession(
    message: ImportSession,
    state: State
): Update<State, Command> =
    state command DoImportSession(message.file)

internal fun exportSessions(
    message: ExportSessions,
    state: State
): Update<State, Command> =
    state command DoExportSessions(message.dir, message.ids.map(state.debugger::component))
