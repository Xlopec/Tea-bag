/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.misc

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.debug.component.DebugEnv
import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonConverter
import com.oliynick.max.tea.core.debug.session.Localhost
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import core.misc.messageAsStateUpdate
import core.misc.throwingResolver
import core.scope.coroutineDispatcher
import io.ktor.http.*
import kotlinx.coroutines.test.TestCoroutineScope

val TestComponentId = ComponentId("test")

val TestSerializer = GsonSerializer()

fun <M, S> TestServerSettings(
    componentId: ComponentId = TestComponentId,
    converter: JsonConverter<JsonElement> = GsonSerializer(),
    url: Url = Localhost,
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
