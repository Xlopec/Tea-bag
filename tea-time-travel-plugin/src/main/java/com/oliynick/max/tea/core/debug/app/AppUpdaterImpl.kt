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

package com.oliynick.max.tea.core.debug.app

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.debug.app.feature.notification.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.feature.notification.updateForNotification
import com.oliynick.max.tea.core.debug.app.feature.presentation.updateForUiMessage
import com.oliynick.max.tea.core.debug.app.feature.server.updateForServerMessage
import com.oliynick.max.tea.core.debug.app.feature.storage.updateForStoreMessage
import com.oliynick.max.tea.core.debug.app.state.State

fun AppUpdater(): AppUpdater = AppUpdaterImpl

private object AppUpdaterImpl : AppUpdater {

    override fun update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is UIMessage -> updateForUiMessage(message, state)
            is NotificationMessage -> updateForNotification(message, state)
            is StoreMessage -> updateForStoreMessage(message, state)
            is ServerMessage -> updateForServerMessage(message, state)
        }

}

internal fun warnUnacceptableMessage(
    message: Message,
    state: State
): UpdateWith<State, Command> =
    state command DoWarnUnacceptableMessage(message, state)
