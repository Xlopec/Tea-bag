@file:Suppress("TestFunctionName")

package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.time.travel.component.DebugEnv
import com.oliynick.max.elm.time.travel.component.ServerSettings
import com.oliynick.max.elm.time.travel.component.SessionBuilder
import com.oliynick.max.elm.time.travel.component.URL
import com.oliynick.max.elm.time.travel.converter.GsonSerializer
import protocol.ComponentId

val testComponentId = ComponentId("test")

val testSerializer = GsonSerializer()

val testServerSettings = ServerSettings(
    testComponentId,
    testSerializer,
    URL()
)

fun <M, C, S> TestEnv(
    env: Env<M, C, S>,
    serverSettings: ServerSettings = testServerSettings,
    testDebugSession: TestDebugSession<M, S>
) = TestEnv(
    env = env,
    serverSettings = serverSettings,
    sessionBuilder = { _, block -> testDebugSession.apply { block() } })

fun <M, C, S> TestEnv(
    env: Env<M, C, S>,
    serverSettings: ServerSettings = testServerSettings,
    sessionBuilder: SessionBuilder<M, S> = { _, block -> TestDebugSession<M, S>().apply { block() } }
) = DebugEnv(
    env,
    serverSettings,
    sessionBuilder
)
