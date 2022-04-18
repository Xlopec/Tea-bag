package com.oliynick.max.tea.core.debug.app.feature.server

import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import java.util.*

data class RemoteCall(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)
