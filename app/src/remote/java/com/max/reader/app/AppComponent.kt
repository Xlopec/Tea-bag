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

/*
package com.max.reader.app

import android.os.Build
import com.max.reader.app.env.Environment
import com.max.reader.app.message.Message
import com.max.reader.app.serialization.PersistentListSerializer
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.ShareStateWhileSubscribed
import com.oliynick.max.tea.core.component.states
import com.oliynick.max.tea.core.debug.component.Component
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import io.ktor.http.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

fun AppComponent(
    environment: Environment,
    initializer: Initializer<AppState, Command>,
): (Flow<Message>) -> Flow<AppState> {

    suspend fun resolve(command: Command) = this.resolve(command)

    fun update(
        message: Message,
        state: AppState,
    ) = this.update(message, state)

    // todo state persistence

    return Component(
        ComponentId("News Reader App: ${Build.MANUFACTURER} ${Build.MODEL}"),
        initializer,
        ::resolve,
        ::update,
        AppGsonSerializer(),
        scope = this,
        io = Dispatchers.IO,
        computation = Dispatchers.Unconfined,
        url = Url("http://10.0.2.2:8080"),
        shareOptions = ShareStateWhileSubscribed
    ).states()
}

private fun AppGsonSerializer() = GsonSerializer {
    registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
}
*/
