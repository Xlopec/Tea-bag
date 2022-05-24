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

package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.Component
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTab
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoView
import io.github.xlopec.tea.time.travel.plugin.feature.storage.ImportSession
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.Platform
import io.github.xlopec.tea.time.travel.plugin.model.Started
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.component
import io.github.xlopec.tea.time.travel.plugin.model.componentIds
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginTheme
import io.kanro.compose.jetbrains.control.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal val LocalPlatform = staticCompositionLocalOf<Platform> { error("No platform implementation provided") }

internal fun PluginSwingAdapter(
    project: Project,
    component: (Flow<Message>) -> Flow<State>,
) = ComposePanel()
    .apply {
        background = null
        setContent {
            PluginTheme {
                val platform = remember { Platform(project) }
                Plugin(platform, component)
            }
        }
    }

@Composable
internal fun Plugin(
    platform: Platform,
    component: (Flow<Message>) -> Flow<State>,
    messages: MutableSharedFlow<Message> = remember { MutableSharedFlow() },
) {
    JPanel(modifier = Modifier.fillMaxSize()) {
        val stateFlow = remember { component(messages) }
        val state = stateFlow.collectAsState(initial = null).value

        CompositionLocalProvider(LocalPlatform provides platform) {
            if (state != null) {
                val scope = rememberCoroutineScope()
                val messageHandler = remember { scope.messageHandlerFor(messages) }

                Plugin(state, messageHandler)
            }
        }
    }
}

@Composable
internal fun Plugin(
    state: State,
    events: MessageHandler,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.weight(2f)
        ) {
            if (state is Started && state.debugState.components.isNotEmpty()) {
                ComponentsView(state, events)
            } else {
                InfoView(state, events)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // settings section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsFields(state, events)

            val scope = rememberCoroutineScope()
            val platform = LocalPlatform.current

            BottomActionMenu(
                state = state,
                events = events,
                onImportSession = { scope.launch { events(ImportSession(platform.chooseSessionFile())) } },
                onExportSession = { started ->
                    scope.launch {
                        platform.chooseExportSessionDirectory(started.debugState.componentIds)?.also(events::invoke)
                    }
                }
            )
        }
    }
}

internal fun CoroutineScope.messageHandlerFor(
    messages: FlowCollector<Message>,
): MessageHandler =
    { message -> launch { messages.emit(message) } }

@Composable
private fun ComponentsView(
    pluginState: Started,
    events: MessageHandler,
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

    Row {
        pluginState.debugState.componentIds.forEachIndexed { index, id ->
            ComponentTab(id, selectedId, pluginState.debugState, index, events)
        }
    }

    Component(pluginState.settings, pluginState.debugState.component(selectedId.value), events)
}
