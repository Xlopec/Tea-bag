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

package com.oliynick.max.tea.core.debug.app.feature.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.TabRow
import androidx.compose.material.TextFieldDefaults.MinHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.oliynick.max.tea.core.debug.app.Message
import com.oliynick.max.tea.core.debug.app.feature.presentation.UpdateServerSettings
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.ActionIcons.Execute
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.ActionIcons.Export
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.ActionIcons.Import
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.ActionIcons.Suspend
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.ValidatedTextField
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.misc.chooseFile
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.tabs.ComponentTab
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.theme.WidgetTheme
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.info.InfoView
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.screens.component.Component
import com.oliynick.max.tea.core.debug.app.feature.server.StartServer
import com.oliynick.max.tea.core.debug.app.feature.server.StopServer
import com.oliynick.max.tea.core.debug.app.feature.storage.ExportSessions
import com.oliynick.max.tea.core.debug.app.feature.storage.ImportSession
import com.oliynick.max.tea.core.debug.app.state.*
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.contracts.contract

fun Plugin(
    project: Project,
    component: (Flow<Message>) -> Flow<State>,
) = ComposePanel()
    .apply {
        background = null
        setContent {
            WidgetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val messages = remember { MutableSharedFlow<Message>() }
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
    messages: FlowCollector<Message>,
): MessageHandler =
    { message -> launch { messages.emit(message) } }

@Composable
private fun Plugin(
    project: Project,
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
                ComponentsView(project, state, events)
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
            BottomActionMenu(project, state, events)
        }
    }
}

@Composable
private fun ComponentsView(
    project: Project,
    pluginState: Started,
    events: MessageHandler
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

    Component(project, pluginState.settings, pluginState.debugState.component(selectedId.value), events)
}

@Composable
private fun SettingsFields(
    state: State,
    events: MessageHandler,
) {
    ValidatedTextField(
        validated = state.settings.host,
        label = "Host:",
        placeholder = "provide host",
        modifier = Modifier.fillMaxWidth().heightIn(28.dp, MinHeight),
        onValueChange = { s ->
            events(UpdateServerSettings(host = s, port = state.settings.port.input))
        },
        enabled = state.canModifySettings
    )

    Spacer(Modifier.height(12.dp))

    ValidatedTextField(
        validated = state.settings.port,
        label = "Port:",
        placeholder = "provide port",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { s ->
            events(UpdateServerSettings(host = state.settings.host.input, port = s))
        },
        enabled = state.canModifySettings
    )
}

@Composable
private fun BottomActionMenu(
    project: Project,
    state: State,
    events: MessageHandler,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        ActionButton(
            enabled = state.canImport(),
            onClick = { project.handlerSessionImport(events) },
            painter = Import,
            contentDescription = "Import session"
        )

        ActionButton(
            enabled = state.canExport(),
            onClick = { project.handleSessionExport(state, events) },
            painter = Export,
            contentDescription = "Export session"
        )

        ActionButton(
            enabled = state.isStarted() || state.canStart(),
            onClick = { events(if (state is Stopped) StartServer else StopServer) },
            painter = state.serverActionIcon,
            contentDescription = "Start/Stop server"
        )
    }
}

val DisabledTintColor = Color(86, 86, 86)

@Composable
private fun ActionButton(
    enabled: Boolean,
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = enabled,
        onClick = onClick
    ) {
        Image(
            colorFilter = if (enabled) null else ColorFilter.tint(DisabledTintColor),
            modifier = Modifier.size(16.dp),
            painter = painter,
            contentDescription = contentDescription
        )
    }
}

private fun Project.handlerSessionImport(
    events: MessageHandler
) {
    chooseImportSessionFile { file ->
        events(ImportSession(file))
    }
}

private fun Project.handleSessionExport(
    state: State,
    events: MessageHandler
) {
    val componentIds = (state as Started).debugState.componentIds
    // if there is no ambiguity regarding what session we should store - don't show chooser popup
    val exportSelection = if (componentIds.size > 1) chooseComponentsForExport(componentIds.toList()) else componentIds

    if (exportSelection.isNotEmpty()) {
        chooseExportSessionDir { dir ->
            events(ExportSessions(exportSelection, dir))
        }
    }
}

private fun State.canExport(): Boolean {
    contract {
        returns(true) implies (this@canExport is Started)
    }

    return isStarted() && debugState.components.isNotEmpty()
}

private fun State.canImport(): Boolean {
    contract {
        returns(true) implies (this@canImport is Started)
    }

    return isStarted()
}

private fun State.isStarted(): Boolean {
    contract {
        returns(true) implies (this@isStarted is Started)
    }
    return this is Started
}

private fun State.canStart(): Boolean {
    contract {
        returns(true) implies (this@canStart is Stopped)
    }
    return this is Stopped && canStart
}


private val State.serverActionIcon: Painter
    @Composable get() = when (this) {
        is Stopped, is Starting -> Execute
        is Started, is Stopping -> Suspend
    }

private val State.canModifySettings
    get() = this is Stopped

private fun Project.chooseExportSessionDir(
    callback: (File) -> Unit
) = chooseFile(
    FileChooserDescriptorFactory.createSingleFolderDescriptor()
        .withFileFilter(VirtualFile::isDirectory)
        .withRoots(listOfNotNull(baseVirtualDir))
        .withTitle("Choose Directory to Save Session"),
    callback
)

private val Project.baseVirtualDir: VirtualFile?
    get() = basePath?.let(LocalFileSystem.getInstance()::findFileByPath)

private fun Project.chooseImportSessionFile(
    callback: (File) -> Unit
) = chooseFile(
    FileChooserDescriptorFactory.createSingleFileDescriptor("json")
        .withRoots(listOfNotNull(baseVirtualDir))
        .withTitle("Choose Session to Import"),
    callback
)

private fun Project.chooseComponentsForExport(
    sessionIds: List<ComponentId>,
): List<ComponentId> {
    val option = Messages.showChooseDialog(
        this,
        "Select which session to export",
        "Export Session",
        null,
        arrayOf("All", *sessionIds.map(ComponentId::value).toTypedArray()),
        "All"
    )

    return when (option) {
        // cancel
        -1 -> listOf()
        // all
        0 -> sessionIds
        else -> sessionIds.subList(option - 1, option)
    }
}
