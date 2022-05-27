package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.ServerMessage
import io.github.xlopec.tea.time.travel.plugin.integration.warnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.Valid

internal fun updateForServerMessage(
    message: ServerMessage,
    state: State
): Update<State, Command> =
    when {
        message === StartServer && state.server !is Server -> startServer(state)
        message === StopServer && state.server is Server -> state command DoStopServer(state.server)
        else -> warnUnacceptableMessage(message, state)
    }

private fun startServer(
    state: State
): Update<State, Command> {

    val host = state.settings.host
    val port = state.settings.port

    return if (host is Valid && port is Valid) {
        state command DoStartServer(ServerAddress(host.t, port.t))
    } else state.noCommand()
}
