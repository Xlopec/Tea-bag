/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.elm.time.travel.app.domain

import protocol.ComponentId
import protocol.Value
import java.io.File

sealed class PluginCommand

data class StoreFiles(val files: List<File>) : PluginCommand()

data class StoreServerSettings(val serverSettings: ServerSettings) : PluginCommand()

data class DoStartServer(val settings: Settings) : PluginCommand()

object DoStopServer : PluginCommand()

data class DoApplyCommand(val id: ComponentId, val command: Value<*>) : PluginCommand()

data class DoApplyState(val id: ComponentId, val state: Value<*>) : PluginCommand()

data class DoNotifyOperationException(val exception: PluginException, val operation: PluginCommand?) : PluginCommand()

