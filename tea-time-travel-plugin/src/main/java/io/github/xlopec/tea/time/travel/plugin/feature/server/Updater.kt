package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.ServerMessage
import io.github.xlopec.tea.time.travel.plugin.integration.onUnhandledMessage
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.Valid

internal fun State.onUpdateForServerMessage(
    message: ServerMessage
): Update<State, Command> =
    when {
        message === StartServer && server !is Server -> onStartServer()
        message === StopServer && server is Server -> onStopServer(server)
        else -> onUnhandledMessage(message)
    }

private fun State.onStopServer(server: Server) =
    this command DoStopServer(server)

private fun State.onStartServer(): Update<State, Command> {

    val host = settings.host
    val port = settings.port

    return if (host is Valid && port is Valid) {
        this command DoStartServer(ServerAddress(host.t, port.t))
    } else noCommand()
}
