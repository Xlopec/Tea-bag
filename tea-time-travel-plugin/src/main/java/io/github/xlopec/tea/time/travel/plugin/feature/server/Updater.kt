package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.Command
import io.github.xlopec.tea.time.travel.plugin.ServerMessage
import io.github.xlopec.tea.time.travel.plugin.domain.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.domain.Valid
import io.github.xlopec.tea.time.travel.plugin.state.Started
import io.github.xlopec.tea.time.travel.plugin.state.Starting
import io.github.xlopec.tea.time.travel.plugin.state.State
import io.github.xlopec.tea.time.travel.plugin.state.Stopped
import io.github.xlopec.tea.time.travel.plugin.state.Stopping
import io.github.xlopec.tea.time.travel.plugin.warnUnacceptableMessage

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
