package com.oliynick.max.tea.core.debug.app.feature.storage

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.StoreMessage
import com.oliynick.max.tea.core.debug.app.state.Started
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.app.state.component
import com.oliynick.max.tea.core.debug.app.warnUnacceptableMessage

internal fun updateForStoreMessage(
    message: StoreMessage,
    state: State,
): UpdateWith<State, Command> = when {
    message is ExportSessions && state is Started -> exportSessions(message, state)
    message is ImportSession && state is Started -> importSession(message, state)
    else -> warnUnacceptableMessage(message, state)
}

internal fun importSession(
    message: ImportSession,
    state: Started
): UpdateWith<State, Command> =
    state command DoImportSession(message.file)

internal fun exportSessions(
    message: ExportSessions,
    state: Started
): UpdateWith<State, Command> =
    state command DoExportSessions(message.dir, message.ids.map(state.debugState::component))


