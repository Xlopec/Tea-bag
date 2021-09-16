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

package com.oliynick.max.tea.core.debug.app.presentation.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.MinHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.Constraints.LAST
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBEditorTabs
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.Validated
import com.oliynick.max.tea.core.debug.app.domain.isValid
import com.oliynick.max.tea.core.debug.app.misc.childScope
import com.oliynick.max.tea.core.debug.app.presentation.component.ComponentView
import com.oliynick.max.tea.core.debug.app.presentation.info.InfoView
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.RunDefaultIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.RunDisabledIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDefaultIcon
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDefaultIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDisabledIcon
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDisabledIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.*
import com.oliynick.max.tea.core.debug.app.presentation.ui.tabs.CloseableTab
import com.oliynick.max.tea.core.debug.app.presentation.ui.theme.WidgetTheme
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.awt.Container
import java.awt.event.MouseEvent
import javax.swing.*
import java.awt.Component as AwtComponent


class ToolWindowView private constructor(
    private val project: Project,
    scope: CoroutineScope,
    private val component: (Flow<PluginMessage>) -> Flow<PluginState>,
) : CoroutineScope by scope {

    companion object {

        fun new(
            project: Project,
            scope: CoroutineScope,
            component: (Flow<PluginMessage>) -> Flow<PluginState>,
        ) = ToolWindowView(project, scope, component).composePanel

    }

    private var panel: JPanel = JPanel()
    private lateinit var startButton: JLabel
    private lateinit var portTextField: JTextField
    private lateinit var hostTextField: JTextField
    private lateinit var componentsPanel: JPanel

    val composePanel = ComposePanel()
        .apply { background = null }

    init {

        val uiEvents = Channel<PluginMessage>()

        composePanel.setContent {
            WidgetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    val stateFlow = remember { component(uiEvents.consumeAsFlow()) }
                    val state = stateFlow.collectAsState(context = Dispatchers.Main, initial = null)

                    state.value?.also { println("new state $it") }?.render1(uiEvents::offer)
                }
            }
        }
    }

    @Composable
    private fun PluginState.render1(
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
                if (this@render1 is Started && debugState.components.isNotEmpty()) {
                    // todo implement component view
                } else {
                    InfoView(this@render1, events)
                }
            }
            // settings section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsFields(this@render1, events)
                BottomActionMenu(this@render1, events)
            }
        }
    }

    @Composable
    private fun SettingsFields(
        pluginState: PluginState,
        events: (PluginMessage) -> Unit,
    ) {
        SettingsInputField(
            setting = pluginState.settings.host,
            label = "Host:",
            placeholder = "provide host",
            enabled = pluginState.canModifySettings,
            modifier = Modifier.fillMaxWidth().heightIn(28.dp, MinHeight),
            onValueChange = { s ->
                events(UpdateServerSettings(host = s, port = pluginState.settings.port.input))
            }
        )

        Spacer(Modifier.height(12.dp))

        SettingsInputField(
            setting = pluginState.settings.port,
            label = "Port:",
            placeholder = "provide port",
            enabled = pluginState.canModifySettings,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { s ->
                events(UpdateServerSettings(host = pluginState.settings.host.input, port = s))
            }
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

    @Composable
    private fun SettingsInputField(
        setting: Validated<*>,
        label: String,
        placeholder: String,
        enabled: Boolean,
        modifier: Modifier = Modifier,
        onValueChange: (newValue: String) -> Unit,
    ) {
        TextField(
            value = setting.input,
            modifier = modifier,
            enabled = enabled,
            //label = { Text(text = label) },
            placeholder = { Text(text = placeholder) },
            isError = !setting.isValid(),
            singleLine = true,
            onValueChange = onValueChange
        )
    }

    private fun render(
        state: Started,
        messages: (PluginMessage) -> Unit,
    ) {
        startButton.icon = SuspendDefaultIcon
        startButton.disabledIcon = SuspendDisabledIcon

        startButton.setOnClickListenerEnabling { messages(StopServer) }

        require(componentsPanel.componentCount == 1) {
            "Invalid components count, children ${componentsPanel.children}"
        }

        if (state.debugState.components.isEmpty()) {
            // show empty view
            if (componentsPanel.first().name != "InfoView.NAME") {
                showEmptyComponentsView()
            }
        } else {

            if (componentsPanel.first().name == "InfoView.NAME") {
                // swap panels
                componentsPanel.removeAll()
                componentsPanel += JBEditorTabs(project)
            }

            (componentsPanel.first() as JBEditorTabs).update(state, messages)
        }

        check(componentsPanel.componentCount == 1) {
            "Invalid components count, children ${componentsPanel.children}"
        }
    }

    private fun showEmptyComponentsView() {
        componentsPanel.removeAll()

        val infoViewScope = childScope()
        TODO("removed")
        //val infoView = InfoView.new(infoViewScope, component.infoViewStates(infoViewScope))

        //componentsPanel += infoView.panel
    }

    private fun JBEditorTabs.update(
        state: Started,
        messages: (PluginMessage) -> Unit,
    ) {

        fun addTab(
            id: ComponentId,
        ) {
            val componentScope = childScope()
            val componentView = ComponentView.new(
                componentScope,
                id,
                project,
                this@ToolWindowView.component.startedStates(id, componentScope),
                state
            )

            addCloseableTab(id, componentView.root, fun(id) = messages(RemoveComponent(id)))
        }

        state.debugState.components
            .filter { (id, _) -> tabs.find { info -> info.`object` == id } == null }
            .forEach { (id, _) -> addTab(id) }
    }

    private fun JBEditorTabs.addCloseableTab(
        id: ComponentId,
        content: JComponent,
        onClose: (ComponentId) -> Unit,
    ) {
        val group = DefaultActionGroup()
        val info = TabInfo(id, content, group)

        group.addAction(CloseableTab(content, fun() { onClose(id); removeTab(info) }), LAST)
        addTabSilently(info, 0)
    }
}

private fun ((Flow<PluginMessage>) -> Flow<PluginState>).startedStates(
    id: ComponentId,
    scope: CoroutineScope,
): ((Flow<PluginMessage>) -> Flow<Started>) = { input ->
    this(input)
        .map { s -> s as? Started }
        .takeWhile { s -> s?.debugState?.components?.containsKey(id) == true }
        .filterNotNull()
        .onCompletion { scope.cancel() }
}

private fun AwtComponent.setOnClickListenerEnabling(l: (MouseEvent) -> Unit) {
    setOnClickListener(l)
    isEnabled = true
}

private fun AwtComponent.removeMouseListenersDisabling() {
    removeMouseListeners()
    isEnabled = false
}

private fun Container.first() = this[0]

private fun TabInfo(
    id: ComponentId,
    content: JComponent,
    group: ActionGroup,
) = TabInfo(content)
    .apply {
        text = id.value
        `object` = id
        setTabLabelActions(group, ActionPlaces.EDITOR_TAB)
    }

private fun JBEditorTabs(
    project: Project,
) = JBEditorTabs(project, null, NoOpDisposable)
    .apply { isTabDraggingEnabled = true }

private suspend fun ((Flow<PluginMessage>) -> Flow<PluginState>).firstState() =
    this(emptyFlow()).first()
