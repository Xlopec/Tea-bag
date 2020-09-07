package com.oliynick.max.tea.core.debug.app.misc

import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.protocol.ComponentId

val TestSettings = Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false)

val StartedTestServerStub = object : Server {
    override suspend fun stop() = Unit
    override suspend fun invoke(component: ComponentId, message: GsonClientMessage) = Unit
}
