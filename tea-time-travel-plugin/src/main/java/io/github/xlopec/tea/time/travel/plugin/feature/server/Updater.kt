package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.ServerMessage
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.model.Valid
import io.github.xlopec.tea.time.travel.plugin.model.Started
import io.github.xlopec.tea.time.travel.plugin.model.Starting
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.model.Stopping
import io.github.xlopec.tea.time.travel.plugin.integration.warnUnacceptableMessage

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
