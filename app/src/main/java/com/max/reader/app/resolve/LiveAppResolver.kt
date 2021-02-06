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

package com.max.reader.app.resolve

import com.max.reader.app.*
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.channels.BroadcastChannel

@Deprecated("wait until it'll be fixed")
fun <Env> AppResolver(): AppResolver<Env> where Env : HasCommandTransport,
                                                Env : ArticlesResolver<Env>,
                                                Env : ArticleDetailsResolver<Env> = object : AppResolver<Env> {
    override suspend fun Env.resolve(command: Command): Set<Message> =
        when (command) {
            is CloseApp -> close(command)
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
        }

    suspend fun Env.close(
        command: CloseApp
    ): Set<Message> = command.sideEffect { closeCommands.send(command) }
}

interface HasCommandTransport {
    val closeCommands: BroadcastChannel<CloseApp>
}

fun CommandTransport() = object : HasCommandTransport {
    override val closeCommands: BroadcastChannel<CloseApp> = BroadcastChannel(1)
}
