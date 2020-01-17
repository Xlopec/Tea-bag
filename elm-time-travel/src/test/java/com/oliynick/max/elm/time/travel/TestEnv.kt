@file:Suppress("TestFunctionName")

package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.time.travel.component.DebugEnv
import com.oliynick.max.elm.time.travel.component.ServerSettings
import com.oliynick.max.elm.time.travel.component.URL
import com.oliynick.max.elm.time.travel.converter.GsonSerializer
import com.oliynick.max.elm.time.travel.converter.JsonConverter
import com.oliynick.max.elm.time.travel.session.SessionBuilder
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
