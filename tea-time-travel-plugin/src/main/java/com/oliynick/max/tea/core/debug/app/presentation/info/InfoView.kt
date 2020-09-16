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

package com.oliynick.max.tea.core.debug.app.presentation.info

import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.presentation.misc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.swing.JLabel
import javax.swing.JPanel

class InfoView private constructor(
    scope: CoroutineScope,
    component: (Flow<PluginMessage>) -> Flow<PluginState>
) : CoroutineScope by scope {

    companion object {
        val NAME = InfoView::class.simpleName!!

        fun new(
            scope: CoroutineScope,
            component: (Flow<PluginMessage>) -> Flow<PluginState>
        ) = InfoView(scope, component)
    }

    lateinit var panel: JPanel
        private set

    private lateinit var messageText: JLabel

    init {
        panel.name = NAME
    }

    init {
        val uiEvents = Channel<PluginMessage>()

        scope.launch {
            component(uiEvents.consumeAsFlow())
                .collect { state -> render(state, uiEvents::offer) }
        }
    }

    private fun render(
        state: PluginState,
        uiEvents: (PluginMessage) -> Unit
    ) =
        when (state) {
            is Stopped -> renderStopped(uiEvents)
            is Started -> renderStarted()
            is Starting, is Stopping -> Unit
        }.safe

    private fun renderStarted() {
        messageText.text = "There are no attached components yet"
        messageText.removeMouseListeners()
    }

    private fun renderStopped(
        messages: (PluginMessage) -> Unit
    ) {
        messageText.text = "Debug server isn't running. Press to start"

        messageText.setOnClickListener {
            messages(StartServer)
        }
    }

}
