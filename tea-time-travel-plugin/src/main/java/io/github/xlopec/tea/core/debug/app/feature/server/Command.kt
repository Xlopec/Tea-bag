package io.github.xlopec.tea.core.debug.app.feature.server

import io.github.xlopec.tea.core.debug.app.ServerCommand
import io.github.xlopec.tea.core.debug.app.domain.ServerAddress
import io.github.xlopec.tea.core.debug.app.domain.Value
import io.github.xlopec.tea.core.debug.app.state.Server
import io.github.xlopec.tea.core.debug.protocol.ComponentId

@JvmInline
value class DoStartServer(
    val address: ServerAddress
) : ServerCommand

@JvmInline
value class DoStopServer(
    val server: Server
) : ServerCommand

data class DoApplyMessage(
    val id: ComponentId,
    val command: Value,
    val server: Server
) : ServerCommand

data class DoApplyState(
    val id: ComponentId,
    val state: Value,
    val server: Server
) : ServerCommand
