@file:Suppress("TestFunctionName")

package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.Env
import protocol.ComponentId

val testComponentId = ComponentId("test")

val testSerializer = gsonSerializer()

val testServerSettings = ServerSettings(
    testComponentId,
    testSerializer,
    URL()
)

fun <M, C, S> TestEnv(
    env: Env<M, C, S>,
    serverSettings: ServerSettings = testServerSettings,
    testDebugSession: TestDebugSession<M, S>
) = TestEnv(env, serverSettings) { testDebugSession }

fun <M, C, S> TestEnv(
    env: Env<M, C, S>,
    serverSettings: ServerSettings = testServerSettings,
    sessionBuilder: SessionBuilder<M, S> = { TestDebugSession() }
) = DebugEnv(
    env,
    serverSettings,
    sessionBuilder
)
