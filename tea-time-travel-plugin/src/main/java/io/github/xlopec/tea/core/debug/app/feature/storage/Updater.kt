package io.github.xlopec.tea.core.debug.app.feature.storage

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.debug.app.Command
import io.github.xlopec.tea.core.debug.app.StoreMessage
import io.github.xlopec.tea.core.debug.app.state.Started
import io.github.xlopec.tea.core.debug.app.state.State
import io.github.xlopec.tea.core.debug.app.state.component
import io.github.xlopec.tea.core.debug.app.warnUnacceptableMessage

internal fun updateForStoreMessage(
    message: StoreMessage,
    state: State,
): Update<State, Command> = when {
    message is ExportSessions && state is Started -> exportSessions(message, state)
    message is ImportSession && state is Started -> importSession(message, state)
    else -> warnUnacceptableMessage(message, state)
}

internal fun importSession(
    message: ImportSession,
    state: Started
): Update<State, Command> =
    state command DoImportSession(message.file)

internal fun exportSessions(
    message: ExportSessions,
    state: Started
): Update<State, Command> =
    state command DoExportSessions(message.dir, message.ids.map(state.debugState::component))


