/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.tea.time.travel.misc

import com.google.gson.JsonElement
import io.github.xlopec.tea.core.Env
import io.github.xlopec.tea.time.travel.component.DebugEnv
import io.github.xlopec.tea.time.travel.component.Settings
import io.github.xlopec.tea.time.travel.gson.GsonSerializer
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.github.xlopec.tea.time.travel.protocol.JsonSerializer
import io.github.xlopec.tea.time.travel.session.Localhost
import io.github.xlopec.tea.time.travel.session.SessionFactory
import io.ktor.http.*

val TestComponentId = ComponentId("test")

val TestSerializer = GsonSerializer()

fun <M, S> TestSettings(
    componentId: ComponentId = TestComponentId,
    converter: JsonSerializer<JsonElement> = GsonSerializer(),
    url: Url = Localhost,
    sessionFactory: SessionFactory<M, S, JsonElement> = { _, block ->
        TestDebugSession<M, S>().apply { block() }
    }
) = Settings(
    componentId,
    converter,
    url,
    sessionFactory
)

fun <M, S, C> TestDebugEnv(
    env: Env<M, S, C>,
    settings: Settings<M, S, JsonElement> = TestSettings()
) = DebugEnv(
    env,
    settings
)
