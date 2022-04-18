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

@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.google.gson.JsonElement
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.serialization.PersistentListSerializer
import com.oliynick.max.reader.app.serialization.PersistentSetSerializer
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareStateWhileSubscribed
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.debug.component.Component
import io.github.xlopec.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonSerializer
import io.ktor.http.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet

fun DebuggableAppComponent(
    environment: Environment,
    initializer: Initializer<AppState, Command>,
): Component<Message, AppState, Command> =
    Component(
        id = ComponentId("News Reader App"),
        initializer = initializer,
        resolver = { c, ctx -> with(environment) { resolve(c, ctx) } },
        updater = { m, s -> with(environment) { update(m, s) } },
        scope = environment,
        url = Url("http://10.0.2.2:8080"),
        jsonSerializer = AppGsonSerializer(),
        shareOptions = ShareStateWhileSubscribed,
    )

private fun AppGsonSerializer(): JsonSerializer<JsonElement> = GsonSerializer {
    registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
    registerTypeAdapter(PersistentSet::class.java, PersistentSetSerializer)
}
