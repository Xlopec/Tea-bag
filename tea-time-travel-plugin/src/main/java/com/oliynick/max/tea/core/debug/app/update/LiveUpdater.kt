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

package com.oliynick.max.tea.core.debug.app.update

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.debug.app.command.Command
import com.oliynick.max.tea.core.debug.app.command.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.message.Message
import com.oliynick.max.tea.core.debug.app.message.NotificationMessage
import com.oliynick.max.tea.core.debug.app.message.UIMessage
import com.oliynick.max.tea.core.debug.app.state.State

fun <Env> LiveUpdater() where Env : NotificationUpdater,
                              Env : UiUpdater =
    object : LiveUpdater<Env> {}

interface LiveUpdater<Env> : Updater<Env> where Env : NotificationUpdater,
                                                Env : UiUpdater {

    override fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is UIMessage -> update(message, state)
            is NotificationMessage -> update(message, state)
        }

}

fun warnUnacceptableMessage(
    message: Message,
    state: State
): UpdateWith<State, Command> =
    state command DoWarnUnacceptableMessage(message, state)
