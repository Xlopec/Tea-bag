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

package io.github.xlopec.tea.core.debug.misc

import com.google.gson.JsonElement
import io.github.xlopec.tea.core.Env
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Resolver
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.ShareStateWhileSubscribed
import io.github.xlopec.tea.core.Updater
import io.github.xlopec.tea.core.debug.component.DebugEnv
import io.github.xlopec.tea.core.debug.component.Settings
import io.github.xlopec.tea.core.debug.gson.GsonSerializer
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.core.debug.protocol.JsonSerializer
import io.github.xlopec.tea.core.debug.session.Localhost
import io.github.xlopec.tea.core.debug.session.SessionFactory
import io.ktor.http.Url
import kotlinx.coroutines.test.TestScope

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

fun <M, S, C> TestScope.TestBaseEnv(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
) = Env(
    initializer,
    resolver,
    updater,
    this,
    shareOptions
)

fun <M, S, C> TestDebugEnv(
    env: Env<M, S, C>,
    settings: Settings<M, S, JsonElement> = TestSettings()
) = DebugEnv(
    env,
    settings
)
