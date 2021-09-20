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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.MinHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.unit.dp
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.Constraints.LAST
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBEditorTabs
import com.jetbrains.rd.util.first
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.Property
import com.oliynick.max.tea.core.debug.app.domain.Validated
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.domain.isValid
import com.oliynick.max.tea.core.debug.app.misc.childScope
import com.oliynick.max.tea.core.debug.app.presentation.component.ComponentView
import com.oliynick.max.tea.core.debug.app.presentation.component.ItemFormatter
import com.oliynick.max.tea.core.debug.app.presentation.component.drawComposeTreeInit
import com.oliynick.max.tea.core.debug.app.presentation.info.InfoView
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.RunDefaultIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.RunDisabledIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.SuspendDefaultIconC
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
import javax.swing.*

class ToolWindowView private constructor(
    private val project: Project,
    private val component: (Flow<PluginMessage>) -> Flow<PluginState>,
) {

    companion object {

        fun new(
            project: Project,
            component: (Flow<PluginMessage>) -> Flow<PluginState>,
        ): JComponent = ToolWindowView(project, component).composePanel

    }

    val composePanel = ComposePanel()
        .apply { background = null }

    init {

        val uiEvents = Channel<PluginMessage>()

        val events: (PluginMessage) -> Unit = { uiEvents.offer(it) }

        composePanel.setContent {
            WidgetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    val stateFlow = remember { component(uiEvents.consumeAsFlow()) }
                    val state = stateFlow.collectAsState(context = Dispatchers.Main, initial = null)

                    state.value?.also { println("new state $it") }?.render1(events)
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

                val t = remember("loh") {  JBEditorTabs(project) }
                val scope = rememberCoroutineScope()

                if (this@render1 is Started && debugState.components.isNotEmpty()) {

                    val (id, state) = debugState.components.first()

                    drawComposeTreeInit(state.state, object : ItemFormatter {
                        override fun format(v: Value): String = toReadableStringShort(v)
                        override fun format(p: Property): String = toReadableStringShort(p)
                    })
                    /*SwingPanel(
                        background = Unspecified,
                        modifier = Modifier.fillMaxSize(),
                        factory = { t },
                        update = { tabs ->

                           tabs.update(this@render1, scope, events)

                        }
                    )*/

                } else {
                    InfoView(this@render1, events)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

    private fun JBEditorTabs.update(
        state: Started,
        scope: CoroutineScope,
        messages: (PluginMessage) -> Unit,
    ) {

        fun addTab(
            id: ComponentId,
        ) {
            val componentScope = scope.childScope()
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
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        isError = !setting.isValid(),
        singleLine = true,
        onValueChange = onValueChange
    )
}

@Composable
@Preview
fun SettingsInputFieldPreview() {
    TextField(
        value = "abc",
        enabled = true,
        placeholder = { Text(text = "placeholder") },
        isError = false,
        singleLine = true,
        onValueChange = {}
    )
}
