/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
