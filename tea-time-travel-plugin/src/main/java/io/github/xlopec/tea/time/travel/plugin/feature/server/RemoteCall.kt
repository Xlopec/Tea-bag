package io.github.xlopec.tea.time.travel.plugin.feature.server

import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlin.uuid.Uuid

data class RemoteCall(
    val callId: Uuid,
    val component: ComponentId,
    val message: GsonClientMessage
)
