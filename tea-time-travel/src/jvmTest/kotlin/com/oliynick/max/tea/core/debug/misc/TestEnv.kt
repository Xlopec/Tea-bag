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
import com.oliynick.max.tea.core.ShareOptions
import com.oliynick.max.tea.core.ShareStateWhileSubscribed
import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.debug.component.ComponentException
import com.oliynick.max.tea.core.debug.component.DebugEnv
import com.oliynick.max.tea.core.debug.component.Settings
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonSerializer
import com.oliynick.max.tea.core.debug.session.Localhost
import com.oliynick.max.tea.core.debug.session.SessionFactory
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
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

fun <M, S, C> TestScope.TestEnv(
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

@OptIn(ExperimentalStdlibApi::class)
val TestScope.coroutineDispatcher: CoroutineDispatcher
    get() = coroutineContext[CoroutineDispatcher.Key]!!

@Suppress("RedundantSuspendModifier")
suspend fun <C> throwingResolver(
    c: C,
): Nothing =
    throw ComponentException("Unexpected command $c")

fun <M, S> throwingUpdater(
    m: M,
    s: S,
): Nothing =
    throw ComponentException("message=$m, state=$s")

fun <S> messageAsStateUpdate(
    message: S,
    @Suppress("UNUSED_PARAMETER") state: S,
): UpdateWith<S, S> =
    message.noCommand()

fun <M, S> messageAsCommand(
    message: M,
    @Suppress("UNUSED_PARAMETER") state: S,
): UpdateWith<S, M> =
    state command message

fun <M, S> ignoringMessageAsStateUpdate(
    message: M,
    @Suppress("UNUSED_PARAMETER") state: S,
): UpdateWith<M, S> =
    message.noCommand()
