package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.util.UUID

data class RemoteCall(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)
