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

import com.oliynick.max.tea.core.debug.app.domain.DebugState
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.isValid
import com.oliynick.max.tea.core.debug.app.transport.Server

sealed class PluginState {
    abstract val settings: Settings
}

data class Stopped(
    override val settings: Settings
) : PluginState() {
    companion object

    val canStart: Boolean = settings.host.isValid() && settings.port.isValid()
}

data class Starting(
    override val settings: Settings
) : PluginState()

data class Started(
    override val settings: Settings,
    val debugState: DebugState,
    val server: Server
) : PluginState()

data class Stopping(
    override val settings: Settings
) : PluginState()
