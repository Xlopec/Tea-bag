@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.misc

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.debug.component.DebugEnv
import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.component.URL
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonConverter
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import core.misc.messageAsStateUpdate
import core.misc.throwingResolver
import core.scope.coroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import java.net.URL

val TestComponentId = ComponentId("test")

val TestSerializer = GsonSerializer()

fun <M, S> TestServerSettings(
    componentId: ComponentId = TestComponentId,
    converter: JsonConverter<JsonElement> = GsonSerializer(),
    url: URL = URL(),
    sessionBuilder: SessionBuilder<M, S, JsonElement> = { _, block ->
        TestDebugSession<M, S>().apply { block() }
    }
) = ServerSettings(
        componentId,
        converter,
        url,
        sessionBuilder
)

fun TestCoroutineScope.TestEnv(
    initializer: Initializer<String, String> = Initializer("")
) = Env(
    initializer,
    ::throwingResolver,
    ::messageAsStateUpdate,
    this,
    coroutineDispatcher,
    coroutineDispatcher
)

fun <M, S, C> TestDebugEnv(
    env: Env<M, S, C>,
    serverSettings: ServerSettings<M, S, JsonElement> = TestServerSettings()
) = DebugEnv(
        env,
        serverSettings
)
