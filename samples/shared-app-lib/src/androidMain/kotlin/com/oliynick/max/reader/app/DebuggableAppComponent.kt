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

@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.google.gson.JsonElement
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.serialization.PersistentListSerializer
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.ShareStateWhileSubscribed
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.debug.component.Component
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonSerializer
import io.ktor.http.*
import kotlinx.collections.immutable.PersistentList

fun DebuggableAppComponent(
    environment: Environment,
    initializer: Initializer<AppState, Command>,
): Component<Message, AppState, Command> =
    Component(
        id = ComponentId("News Reader App"),
        initializer = initializer,
        resolver = { c -> with(environment) { resolve(c) } },
        updater = { m, s -> with(environment) { update(m, s) } },
        scope = environment,
        url = Url("http://10.0.2.2:8080"),
        jsonSerializer = AppGsonSerializer(),
        shareOptions = ShareStateWhileSubscribed,
    )

private fun AppGsonSerializer(): JsonSerializer<JsonElement> = GsonSerializer {
    registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
}
