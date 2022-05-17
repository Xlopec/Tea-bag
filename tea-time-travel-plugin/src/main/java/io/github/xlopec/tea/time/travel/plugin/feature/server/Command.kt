package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.time.travel.plugin.integration.ServerCommand
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.model.Server
import io.github.xlopec.tea.time.travel.protocol.ComponentId

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
