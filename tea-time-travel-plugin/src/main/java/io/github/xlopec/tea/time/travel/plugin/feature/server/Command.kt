package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.time.travel.plugin.ServerCommand
import io.github.xlopec.tea.time.travel.plugin.domain.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.domain.Value
import io.github.xlopec.tea.time.travel.plugin.state.Server

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
