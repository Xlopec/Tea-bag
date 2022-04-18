package io.github.xlopec.tea.core.debug.app.feature.server

import io.github.xlopec.tea.core.debug.gson.GsonClientMessage
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import java.util.UUID

data class RemoteCall(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)
