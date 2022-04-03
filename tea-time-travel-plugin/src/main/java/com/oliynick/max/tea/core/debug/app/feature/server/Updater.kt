package com.oliynick.max.tea.core.debug.app.feature.server

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.ServerMessage
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.app.state.*
import com.oliynick.max.tea.core.debug.app.warnUnacceptableMessage

internal fun updateForServerMessage(
    message: ServerMessage,
    state: State
): UpdateWith<State, Command> =
    when {
        message === StartServer && state is Stopped -> startServer(state)
        message === StopServer && state is Started -> stopServer(state)
        else -> warnUnacceptableMessage(message, state)
    }

private fun startServer(
    state: Stopped
): UpdateWith<State, Command> {

    val host = state.settings.host
    val port = state.settings.port

    return if (host is Valid && port is Valid) {
        Starting(state.settings) command DoStartServer(ServerAddress(host.t, port.t))
    } else state.noCommand()
}

private fun stopServer(
    state: Started
): UpdateWith<Stopping, DoStopServer> =
    Stopping(state.settings) command DoStopServer(state.server)
