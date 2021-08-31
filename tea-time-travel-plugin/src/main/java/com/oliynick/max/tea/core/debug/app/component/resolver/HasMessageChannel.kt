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

package com.oliynick.max.tea.core.debug.app.component.resolver

import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import kotlinx.coroutines.channels.BroadcastChannel

fun HasMessagesChannel(
    events: BroadcastChannel<PluginMessage> = BroadcastChannel(1)
) = object : HasMessageChannel {
    override val events = events
}

interface HasMessageChannel {
    val events: BroadcastChannel<PluginMessage>
}
