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

package com.oliynick.max.tea.core.debug.app.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.TabRow
import androidx.compose.material.TextFieldDefaults.MinHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.presentation.info.InfoView
import com.oliynick.max.tea.core.debug.app.presentation.screens.component.Component
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.RunDefaultIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.RunDisabledIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDefaultIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDisabledIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValidatedTextField
import com.oliynick.max.tea.core.debug.app.presentation.ui.tabs.ComponentTab
import com.oliynick.max.tea.core.debug.app.presentation.ui.theme.WidgetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

fun Plugin(
    project: Project,
    component: (Flow<PluginMessage>) -> Flow<PluginState>,
) = ComposePanel()
    .apply {
        background = null
        setContent {
            WidgetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val messages = remember { MutableSharedFlow<PluginMessage>() }
                    val stateFlow = remember { component(messages) }
                    val state = stateFlow.collectAsState(context = Main, initial = null).value

                    if (state != null) {
                        val scope = rememberCoroutineScope()
                        val messageHandler = remember { scope.dispatcher(messages) }

                        Plugin(project, state, messageHandler)
                    }
                }
            }
        }
    }

fun CoroutineScope.dispatcher(
    messages: FlowCollector<PluginMessage>,
): (PluginMessage) -> Unit =
    { message -> launch { messages.emit(message) } }

@Composable
private fun Plugin(
    project: Project,
    pluginState: PluginState,
    events: (PluginMessage) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.weight(2f)
        ) {
            if (pluginState is Started && pluginState.debugState.components.isNotEmpty()) {
                ComponentsView(project, pluginState, events)
            } else {
                InfoView(pluginState, events)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // settings section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsFields(pluginState, events)
            BottomActionMenu(pluginState, events)
        }
    }
}

@Composable
private fun ComponentsView(
    project: Project,
    pluginState: Started,
    events: (PluginMessage) -> Unit
) {
    require(pluginState.debugState.components.isNotEmpty())

    val selectedId = remember { mutableStateOf(pluginState.debugState.components.keys.first()) }
    val selectedIndex by derivedStateOf { pluginState.debugState.components.keys.indexOf(selectedId.value) }

    require(selectedIndex >= 0) {
        """Inconsistency in tab indexing detected, 
                            |selected id: ${selectedId.value}, 
                            |selected index: $selectedIndex
                            |component ids: ${pluginState.debugState.componentIds}"""".trimMargin()
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        backgroundColor = Unspecified
    ) {
        pluginState.debugState.componentIds.forEachIndexed { index, id ->
            ComponentTab(id, selectedId, pluginState.debugState, index, events)
        }
    }

    Component(project, pluginState.debugState.component(selectedId.value), events)
}

@Composable
private fun SettingsFields(
    pluginState: PluginState,
    events: (PluginMessage) -> Unit,
) {
    ValidatedTextField(
        validated = pluginState.settings.host,
        label = "Host:",
        placeholder = "provide host",
        modifier = Modifier.fillMaxWidth().heightIn(28.dp, MinHeight),
        onValueChange = { s ->
            events(UpdateServerSettings(host = s, port = pluginState.settings.port.input))
        },
        enabled = pluginState.canModifySettings
    )

    Spacer(Modifier.height(12.dp))

    ValidatedTextField(
        validated = pluginState.settings.port,
        label = "Port:",
        placeholder = "provide port",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { s ->
            events(UpdateServerSettings(host = pluginState.settings.host.input, port = s))
        },
        enabled = pluginState.canModifySettings
    )
}

@Composable
private fun BottomActionMenu(
    state: PluginState,
    events: (PluginMessage) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            enabled = (state is Stopped && state.canStart) || state is Started,
            onClick = { events(if (state is Stopped) StartServer else StopServer) }
        ) {
            Image(
                modifier = Modifier.size(16.dp),
                bitmap = state.toBottomActionIcon(),
                contentDescription = "Action button"
            )
        }
    }
}

@Composable
private fun PluginState.toBottomActionIcon() =
    when (this) {
        is Stopped -> if (canStart) RunDefaultIconC else RunDisabledIconC
        is Starting -> RunDisabledIconC
        is Started -> SuspendDefaultIconC
        is Stopping -> SuspendDisabledIconC
    }

private val PluginState.canModifySettings
    get() = this is Stopped
