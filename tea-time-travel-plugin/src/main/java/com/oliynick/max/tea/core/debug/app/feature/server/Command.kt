package com.oliynick.max.tea.core.debug.app.feature.server

import com.oliynick.max.tea.core.debug.app.ServerCommand
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.state.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

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
