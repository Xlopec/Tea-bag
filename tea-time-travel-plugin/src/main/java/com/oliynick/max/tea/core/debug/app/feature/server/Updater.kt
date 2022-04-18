package com.oliynick.max.tea.core.debug.app.feature.server

import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.ServerMessage
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.app.state.Started
import com.oliynick.max.tea.core.debug.app.state.Starting
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.app.state.Stopped
import com.oliynick.max.tea.core.debug.app.state.Stopping
import com.oliynick.max.tea.core.debug.app.warnUnacceptableMessage
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand

internal fun updateForServerMessage(
    message: ServerMessage,
    state: State
): Update<State, Command> =
    when {
        message === StartServer && state is Stopped -> startServer(state)
        message === StopServer && state is Started -> stopServer(state)
        else -> warnUnacceptableMessage(message, state)
    }

private fun startServer(
    state: Stopped
): Update<State, Command> {

    val host = state.settings.host
    val port = state.settings.port

    return if (host is Valid && port is Valid) {
        Starting(state.settings) command DoStartServer(ServerAddress(host.t, port.t))
    } else state.noCommand()
}

private fun stopServer(
    state: Started
): Update<Stopping, DoStopServer> =
    Stopping(state.settings) command DoStopServer(state.server)
