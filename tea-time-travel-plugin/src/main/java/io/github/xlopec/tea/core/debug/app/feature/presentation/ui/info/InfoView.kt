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

package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign.Companion.Justify
import io.github.xlopec.tea.core.debug.app.domain.Invalid
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.modifier.noIndicationClickable
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.screens.component.MessageHandler
import io.github.xlopec.tea.core.debug.app.feature.server.StartServer
import io.github.xlopec.tea.core.debug.app.state.Started
import io.github.xlopec.tea.core.debug.app.state.Starting
import io.github.xlopec.tea.core.debug.app.state.State
import io.github.xlopec.tea.core.debug.app.state.Stopped
import io.github.xlopec.tea.core.debug.app.state.Stopping
import java.util.Locale

@Composable
fun InfoView(
    state: State,
    uiEvents: MessageHandler,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is Stopped -> InfoViewMessage(state.toDescription(),
                uiEvents.takeIf { state.canStart })
            is Started -> InfoViewMessage(state.toDescription())
            is Starting -> InfoViewMessage(state.toDescription())
            is Stopping -> InfoViewMessage(state.toDescription())
        }
    }
}

@Composable
private fun InfoViewMessage(
    description: String,
    messages: (MessageHandler)? = null,
) {
    Text(
        text = description,
        textAlign = Justify,
        modifier = if (messages == null) Modifier else Modifier.noIndicationClickable {
            messages(StartServer)
        }
    )
}

private fun Started.toDescription(): String {
    require(debugState.components.isEmpty()) { "Non empty debug state, component=${debugState.components}" }
    return "There are no attached components yet"
}

private fun Starting.toDescription() = "Debug server is starting on " +
        "${settings.host.input}:${settings.port.input}"

private fun Stopped.toDescription() =
    if (canStart) "Debug server isn't running. Press to start"
    else "Can't start debug server: ${
        listOf(settings.port, settings.host)
            .filterIsInstance<Invalid>()
            .joinToString(postfix = "\n") { v -> v.message.replaceFirstChar { it.lowercase(Locale.getDefault()) } }
    }"

private fun Stopping.toDescription() = "Debug server is stopping on " +
        "${settings.host.input}:${settings.port.input}"
