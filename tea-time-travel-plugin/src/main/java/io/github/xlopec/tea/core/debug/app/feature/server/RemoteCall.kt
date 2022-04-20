package io.github.xlopec.tea.core.debug.app.feature.server

import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import java.util.UUID

data class RemoteCall(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)
