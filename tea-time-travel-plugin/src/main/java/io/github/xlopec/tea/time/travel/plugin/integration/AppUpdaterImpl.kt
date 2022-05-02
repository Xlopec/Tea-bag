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

package io.github.xlopec.tea.time.travel.plugin.integration

import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.time.travel.plugin.feature.notification.DoWarnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.feature.notification.updateForNotification
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.updateForUiMessage
import io.github.xlopec.tea.time.travel.plugin.feature.server.updateForServerMessage
import io.github.xlopec.tea.time.travel.plugin.feature.storage.updateForStoreMessage
import io.github.xlopec.tea.time.travel.plugin.model.State

fun AppUpdater(): AppUpdater = AppUpdater { message, state ->
    when (message) {
        is ComponentMessage -> updateForUiMessage(message, state)
        is NotificationMessage -> updateForNotification(message, state)
        is StoreMessage -> updateForStoreMessage(message, state)
        is ServerMessage -> updateForServerMessage(message, state)
    }
}

internal fun warnUnacceptableMessage(
    message: Message,
    state: State
): Update<State, Command> =
    state command DoWarnUnacceptableMessage(message, state)
