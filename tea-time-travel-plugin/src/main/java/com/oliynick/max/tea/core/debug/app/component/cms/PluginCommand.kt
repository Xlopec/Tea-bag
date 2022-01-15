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

package com.oliynick.max.tea.core.debug.app.component.cms

import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

sealed interface PluginCommand

data class DoStoreSettings(
    val settings: Settings
) : PluginCommand

data class DoStartServer(
    val address: ServerAddress
) : PluginCommand

data class DoStopServer(
    val server: Server
) : PluginCommand

data class DoApplyMessage(
    val id: ComponentId,
    val command: Value,
    val server: Server
) : PluginCommand

data class DoApplyState(
    val id: ComponentId,
    val state: Value,
    val server: Server
) : PluginCommand

data class DoNotifyOperationException(
    val exception: PluginException,
    val operation: PluginCommand?
) : PluginCommand

data class DoWarnUnacceptableMessage(
    val message: PluginMessage,
    val state: PluginState
) : PluginCommand

data class DoNotifyComponentAttached(
    val componentId: ComponentId
) : PluginCommand
