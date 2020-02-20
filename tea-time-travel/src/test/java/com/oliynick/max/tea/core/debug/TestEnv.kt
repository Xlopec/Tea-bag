@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.debug.component.DebugEnv
import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.component.URL
import com.oliynick.max.tea.core.debug.converter.GsonSerializer
import com.oliynick.max.tea.core.debug.converter.JsonConverter
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import protocol.ComponentId
import java.net.URL

val testComponentId = ComponentId("test")

val testSerializer = GsonSerializer()

fun <M, S> TestServerSettings(
    componentId: ComponentId = testComponentId,
    converter: JsonConverter =  GsonSerializer(),
    url: URL = URL(),
    sessionBuilder: SessionBuilder<M, S> = { _, block -> TestDebugSession<M, S>().apply { block() } }
) = ServerSettings(
    componentId,
    converter,
    url,
    sessionBuilder
)

fun <M, C, S> TestEnv(
    env: Env<M, C, S>,
    serverSettings: ServerSettings<M, S> = TestServerSettings()
) = DebugEnv(
    env,
    serverSettings
)
