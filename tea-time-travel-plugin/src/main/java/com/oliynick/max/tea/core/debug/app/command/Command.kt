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

package com.oliynick.max.tea.core.debug.app.command

import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.message.Message
import com.oliynick.max.tea.core.debug.app.resolve.PluginException
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

sealed interface Command

@JvmInline
value class DoStoreSettings(
    val settings: Settings
) : Command

@JvmInline
value class DoStartServer(
    val address: ServerAddress
) : Command

@JvmInline
value class DoStopServer(
    val server: Server
) : Command

data class DoApplyMessage(
    val id: ComponentId,
    val command: Value,
    val server: Server
) : Command

data class DoApplyState(
    val id: ComponentId,
    val state: Value,
    val server: Server
) : Command

data class DoNotifyOperationException(
    val exception: PluginException,
    val operation: Command?
) : Command

data class DoWarnUnacceptableMessage(
    val message: Message,
    val state: State
) : Command

@JvmInline
value class DoNotifyComponentAttached(
    val componentId: ComponentId
) : Command
