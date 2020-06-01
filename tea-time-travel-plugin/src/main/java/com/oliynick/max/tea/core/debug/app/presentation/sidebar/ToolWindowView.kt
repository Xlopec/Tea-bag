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

package com.oliynick.max.tea.core.debug.app.presentation.sidebar

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.Validated
import com.oliynick.max.tea.core.debug.app.domain.isValid
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.app.presentation.component.ComponentView
import com.oliynick.max.tea.core.debug.app.presentation.info.InfoView
import com.oliynick.max.tea.core.debug.app.presentation.misc.*
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.CloseDarkIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.CloseDefaultIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.ResumeIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.RunDefaultIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.RunDisabledIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.StoppingIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.SuspendDefaultIcon
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.SuspendDisabledIcon
import com.oliynick.max.tea.core.debug.app.presentation.ui.ErrorColor
import com.oliynick.max.tea.core.debug.app.presentation.ui.InputTimeoutMillis
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.awt.Container
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import javax.swing.*
import java.awt.Component as AwtComponent

class ToolWindowView private constructor(
    private val project: Project,
    scope: CoroutineScope,
    private val component: (Flow<PluginMessage>) -> Flow<PluginState>
) : CoroutineScope by scope {

    companion object {

        fun new(
            project: Project,
            scope: CoroutineScope,
            component: (Flow<PluginMessage>) -> Flow<PluginState>
        ) = ToolWindowView(project, scope, component).panel

    }

    private lateinit var panel: JPanel
    private lateinit var startButton: JLabel
    private lateinit var portTextField: JTextField
    private lateinit var hostTextField: JTextField
    private lateinit var componentsPanel: JPanel

    val root: JPanel get() = panel

    private val idToScope = mutableMapOf<Any, CoroutineScope>()

    init {

        launch {
            val uiEvents = Channel<PluginMessage>()

            component(uiEvents.consumeAsFlow().mergeWith(updateServerSettings(component.firstState())))
                .collect { state -> render(state, uiEvents::offer) }
        }
    }

    private fun updateServerSettings(
        initial: PluginState
    ) =
        hostInputChanges(initial).combine(portInputChanges(initial)) { host, port -> UpdateServerSettings(host, port) }
            .distinctUntilChanged()
            .debounce(InputTimeoutMillis)

    private fun hostInputChanges(
        initial: PluginState
    ) = hostTextField.textChanges().onStart(initial.settings.host.input)

    private fun portInputChanges(
        initial: PluginState
    ) = portTextField.textChanges().onStart(initial.settings.port.input)

    private fun render(
        state: PluginState,
        messages: (PluginMessage) -> Unit
    ) {

        portTextField.isEnabled = state is Stopped
        hostTextField.isEnabled = portTextField.isEnabled

        val serverSettings = state.settings

        hostTextField.updateErrorMessage(serverSettings.host)
        portTextField.updateErrorMessage(serverSettings.port)

        hostTextField.textSafe = serverSettings.host.input
        portTextField.textSafe = serverSettings.port.input

        when (state) {
            is Stopped -> render(state, messages)
            is Starting -> render(state)
            is Started -> render(state, messages)
            is Stopping -> render(state)
        }.safe
    }

    private fun render(
        state: Stopped,
        messages: (PluginMessage) -> Unit
    ) {

        startButton.icon = RunDefaultIcon
        startButton.disabledIcon = RunDisabledIcon

        if (state.canStart) {
            startButton.setOnClickListenerEnabling { messages(StartServer) }
        } else {
            startButton.removeMouseListenersDisabling()
        }

        val shouldRemoveOrEmpty = componentsPanel.isEmpty || (componentsPanel.isNotEmpty && componentsPanel.first().name != InfoView.NAME)

        if (shouldRemoveOrEmpty) {
            showEmptyComponentsView()
        }

        check(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children}" }
    }

    private fun render(
        @Suppress("UNUSED_PARAMETER") state: Starting
    ) {
        startButton.icon = RunDisabledIcon
        startButton.disabledIcon = ResumeIcon

        startButton.removeMouseListenersDisabling()
    }

    private fun render(
        state: Started,
        messages: (PluginMessage) -> Unit
    ) {
        startButton.icon = SuspendDefaultIcon
        startButton.disabledIcon = SuspendDisabledIcon

        startButton.setOnClickListenerEnabling { messages(StopServer) }

        require(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children}" }

        if (state.debugState.components.isEmpty()) {
            // show empty view
            if (componentsPanel.first().name != InfoView.NAME) {
                showEmptyComponentsView()
            }
        } else {

            if (componentsPanel.first().name == InfoView.NAME) {
                // swap panels
                componentsPanel.removeAll()
                componentsPanel += tabbedComponentsView()
            }

            (componentsPanel.first() as JTabbedPane).update(state, messages)
        }

        check(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children}" }
    }

    private fun render(@Suppress("UNUSED_PARAMETER") state: Stopping) {
        startButton.icon = SuspendDefaultIcon
        startButton.disabledIcon = StoppingIcon

        startButton.removeMouseListenersDisabling()
    }

    private fun showEmptyComponentsView() {
        componentsPanel.removeAll()

        val infoView = InfoView.new(childScope(), component)

        this[InfoView.NAME] = infoView
        componentsPanel += infoView.panel
    }

    private fun tabbedComponentsView() = JBTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
        .also { tabPane -> tabPane.model = DefaultSingleSelectionModel() }

    private fun JTabbedPane.update(
        state: Started,
        messages: (PluginMessage) -> Unit
    ) {

        fun closeHandler(
            id: ComponentId
        ) {
            this@ToolWindowView -= id
            messages(RemoveComponent(id))
        }

        fun addTab(
            id: ComponentId
        ) {
            val componentView = ComponentView.new(childScope(), id, component.startedStates(), state)

            this@ToolWindowView[id] = componentView
            addCloseableTab(id, componentView.root, ::closeHandler)
        }

        state.debugState.components
            .filter { e -> indexOfTab(e.key.id) == -1 }
            .forEach { (id, _) -> addTab(id) }
    }

    private operator fun set(
        key: Any,
        scope: CoroutineScope
    ) = idToScope.compute(key) { _, old -> old?.cancel(); scope }

    private operator fun minusAssign(
        key: Any
    ) {
        idToScope.remove(key)?.cancel()
    }

}

private fun ((Flow<PluginMessage>) -> Flow<PluginState>).startedStates() =
    mapNullableS { pluginState -> pluginState as? Started }

private fun AwtComponent.setOnClickListenerEnabling(l: (MouseEvent) -> Unit) {
    setOnClickListener(l)
    isEnabled = true
}

private fun AwtComponent.removeMouseListenersDisabling() {
    removeMouseListeners()
    isEnabled = false
}

private inline fun JTabbedPane.addCloseableTab(
    component: ComponentId,
    content: AwtComponent,
    crossinline onClose: (ComponentId) -> Unit
) {
    addTab(component.id, content)

    val panel = JPanel(FlowLayout()).apply {
        isOpaque = false

        add(JLabel(component.id, SwingConstants.LEADING))
        add(JLabel(CloseDefaultIcon).apply {
            setHover(CloseDarkIcon)
            setOnClickListener { onClose(component) }
        })
    }

    setTabComponentAt(indexOfComponent(content), panel)
}

private fun JTextField.updateErrorMessage(
    validated: Validated<*>
) {
    if (validated.isValid()) {
        background = null
        toolTipText = null
    } else {
        background = ErrorColor
        toolTipText = validated.message
    }
}

private fun Container.first() = this[0]

private suspend fun ((Flow<PluginMessage>) -> Flow<PluginState>).firstState() = this(emptyFlow()).first()
