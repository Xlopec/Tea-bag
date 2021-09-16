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

package com.oliynick.max.tea.core.debug.app.presentation.info

import androidx.compose.desktop.ComposePanel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion.Justify
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.Invalid
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.safe
import com.oliynick.max.tea.core.debug.app.presentation.ui.modifier.noIndicationClickable
import com.oliynick.max.tea.core.debug.app.presentation.ui.theme.WidgetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import java.util.*

class InfoView private constructor(
    scope: CoroutineScope,
    component: (Flow<PluginMessage>) -> Flow<PluginState>,
) : CoroutineScope by scope {

    companion object {
        val NAME = InfoView::class.simpleName!!

        fun new(
            scope: CoroutineScope,
            component: (Flow<PluginMessage>) -> Flow<PluginState>,
        ) = InfoView(scope, component)
    }

    val panel = ComposePanel()
        .also { p -> p.name = NAME }

    init {
        val uiEvents = Channel<PluginMessage>()

        panel.background = null
        panel.setContent {

            val stateFlow = remember { component(uiEvents.consumeAsFlow()) }
            val state = stateFlow.collectAsState(context = Dispatchers.Main, initial = null)

            WidgetTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.value?.let { s -> InfoView(s, uiEvents::offer) }
                    }
                }
            }
        }
    }

    @Composable
    private fun InfoView(
        state: PluginState,
        uiEvents: (PluginMessage) -> Unit,
    ) {
        when (state) {
            is Stopped -> InfoViewMessage(state.toDescription(), uiEvents.takeIf { state.canStart })
            is Started -> InfoViewMessage(state.toDescription())
            is Starting -> InfoViewMessage(state.toDescription())
            is Stopping -> InfoViewMessage(state.toDescription())
        }.safe
    }

    @Composable
    private fun InfoViewMessage(
        description: String,
        messages: ((PluginMessage) -> Unit)? = null,
    ) {
        Text(
            text = description,
            textAlign = Justify,
            modifier = if (messages == null) Modifier else Modifier.noIndicationClickable {
                messages(StartServer)
            }
        )
    }
}

private fun Started.toDescription() = "There are no attached components yet"

private fun Starting.toDescription() = "Debug server is starting on " +
        "${settings.host.input}:${settings.port.input}"

private fun Stopped.toDescription() =
    if (canStart) "Debug server isn't running. Press to start"
    else "Can't start debug server: ${
        listOf(settings.port, settings.host)
            .filterIsInstance<Invalid>()
            .joinToString(postfix = "\n") { v -> v.message.decapitalize(Locale.getDefault()) }
    }"

private fun Stopping.toDescription() = "Debug server is stopping on " +
        "${settings.host.input}:${settings.port.input}"
