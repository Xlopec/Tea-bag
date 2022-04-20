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

package io.github.xlopec.tea.time.travel.plugin

import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.data.fold
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotificationResolver
import io.github.xlopec.tea.time.travel.plugin.feature.server.ServerCommandResolver
import io.github.xlopec.tea.time.travel.plugin.feature.storage.StorageResolver

fun <Env> AppResolver(): AppResolver<Env> where Env : ServerCommandResolver,
                                                Env : StorageResolver,
                                                Env : NotificationResolver = AppResolverImpl()

private class AppResolverImpl<Env> :
    AppResolver<Env> where Env : ServerCommandResolver, Env : StorageResolver, Env : NotificationResolver {

    override fun Env.resolve(
        command: Command,
        ctx: ResolveCtx<Message>
    ) {
        ctx.effects {
            when (command) {
                is NotifyCommand -> resolve(command)
                is ServerCommand -> resolveServerCommand(command)
                is StoreCommand -> resolveStoreCommand(command)
            }.fold(::setOfNotNull, ::setOf)
        }
    }

}
