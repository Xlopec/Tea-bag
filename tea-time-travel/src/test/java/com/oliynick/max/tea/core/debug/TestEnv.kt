@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.debug.component.*
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonConverter
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import java.net.URL

val testComponentId = ComponentId("test")

val testSerializer = GsonSerializer()

fun <M, S> TestServerSettings(
    componentId: ComponentId = testComponentId,
    converter: JsonConverter<JsonElement> = GsonSerializer(),
    url: URL = URL(),
    sessionBuilder: SessionBuilder<M, S, JsonElement> = { _, block -> TestDebugSession<M, S>().apply { block() } }
) = ServerSettings(
        componentId,
        converter,
        url,
        sessionBuilder
)

fun <M, S, C> TestEnv(
    env: Env<M, S, C>,
    serverSettings: ServerSettings<M, S, JsonElement> = TestServerSettings()
) = DebugEnv(
        env,
        serverSettings
)
