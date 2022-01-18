package com.oliynick.max.tea.core.debug.app.transport

import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import java.util.*

data class RemoteCall(
    val callId: UUID,
    val component: ComponentId,
    val message: GsonClientMessage
)
