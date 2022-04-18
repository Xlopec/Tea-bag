package com.oliynick.max.tea.core.debug.app.feature.storage

import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.StoreMessage
import com.oliynick.max.tea.core.debug.app.state.Started
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.app.state.component
import com.oliynick.max.tea.core.debug.app.warnUnacceptableMessage
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command

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


