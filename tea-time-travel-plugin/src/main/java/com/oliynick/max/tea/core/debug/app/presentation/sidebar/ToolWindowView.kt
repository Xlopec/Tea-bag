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
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.component.cms.PluginState
import com.oliynick.max.tea.core.debug.app.component.cms.RemoveComponent
import com.oliynick.max.tea.core.debug.app.component.cms.StartServer
import com.oliynick.max.tea.core.debug.app.component.cms.Started
import com.oliynick.max.tea.core.debug.app.component.cms.Starting
import com.oliynick.max.tea.core.debug.app.component.cms.StopServer
import com.oliynick.max.tea.core.debug.app.component.cms.Stopped
import com.oliynick.max.tea.core.debug.app.component.cms.Stopping
import com.oliynick.max.tea.core.debug.app.component.cms.UpdateHost
import com.oliynick.max.tea.core.debug.app.component.cms.UpdatePort
import com.oliynick.max.tea.core.debug.app.misc.mapNullableS
import com.oliynick.max.tea.core.debug.app.presentation.component.ComponentView
import com.oliynick.max.tea.core.debug.app.presentation.info.InfoView
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.CLOSE_DARK_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.CLOSE_DEFAULT_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.RESUME_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.RUN_DEFAULT_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.RUN_DISABLED_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.STOPPING_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.SUSPEND_DEFAULT_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.SUSPEND_DISABLED_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.DefaultDocumentListener
import com.oliynick.max.tea.core.debug.app.presentation.misc.children
import com.oliynick.max.tea.core.debug.app.presentation.misc.get
import com.oliynick.max.tea.core.debug.app.presentation.misc.isEmpty
import com.oliynick.max.tea.core.debug.app.presentation.misc.isNotEmpty
import com.oliynick.max.tea.core.debug.app.presentation.misc.plusAssign
import com.oliynick.max.tea.core.debug.app.presentation.misc.removeMouseListeners
import com.oliynick.max.tea.core.debug.app.presentation.misc.safe
import com.oliynick.max.tea.core.debug.app.presentation.misc.setHover
import com.oliynick.max.tea.core.debug.app.presentation.misc.setOnClickListener
import com.oliynick.max.tea.core.debug.app.presentation.misc.textSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import protocol.ComponentId
import java.awt.Container
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import javax.swing.DefaultSingleSelectionModel
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTextField
import javax.swing.SwingConstants
import java.awt.Component as AwtComponent

class ToolWindowView(
    private val project: Project,
    scope: CoroutineScope,
    private val component: (Flow<PluginMessage>) -> Flow<PluginState>
) : CoroutineScope by scope {

    private lateinit var panel: JPanel
    private lateinit var startButton: JLabel
    private lateinit var portTextField: JTextField
    private lateinit var hostTextField: JTextField
    private lateinit var componentsPanel: JPanel

    private val uiEvents = BroadcastChannel<PluginMessage>(1)

    val root: JPanel get() = panel

    init {

        portTextField.document.addDocumentListener(DefaultDocumentListener { value ->
            uiEvents.offer(UpdatePort(value.toUIntOrNull() ?: return@DefaultDocumentListener))
        })

        hostTextField.document.addDocumentListener(DefaultDocumentListener { value ->
            uiEvents.offer(UpdateHost(value))
        })

        launch { component(uiEvents.asFlow()).collect { state -> render(state, uiEvents::offer) } }
    }

    private fun render(
        state: PluginState,
        messages: (PluginMessage) -> Unit
    ) {

        portTextField.isEnabled = state is Stopped
        hostTextField.isEnabled = portTextField.isEnabled

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
        portTextField.textSafe = state.settings.serverSettings.port.toString()
        hostTextField.textSafe = state.settings.serverSettings.host

        startButton.icon = RUN_DEFAULT_ICON
        startButton.disabledIcon = RUN_DISABLED_ICON

        startButton.setOnClickListenerEnabling { messages(StartServer) }

        val shouldRemoveOrEmpty = componentsPanel.isEmpty || (componentsPanel.isNotEmpty && componentsPanel.first().name != InfoView.NAME)

        if (shouldRemoveOrEmpty) {
            showEmptyComponentsView()
        }

        check(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children}" }
    }

    private fun render(@Suppress("UNUSED_PARAMETER") state: Starting) {
        startButton.icon = RUN_DISABLED_ICON
        startButton.disabledIcon = RESUME_ICON

        startButton.removeMouseListenersDisabling()
    }

    private fun render(
        state: Started,
        messages: (PluginMessage) -> Unit
    ) {
        startButton.icon = SUSPEND_DEFAULT_ICON
        startButton.disabledIcon = SUSPEND_DISABLED_ICON

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
        startButton.icon = SUSPEND_DEFAULT_ICON
        startButton.disabledIcon = STOPPING_ICON

        startButton.removeMouseListenersDisabling()
    }

    private fun showEmptyComponentsView() {
        componentsPanel.removeAll()
        componentsPanel += InfoView(component, coroutineContext).root
    }

    private fun tabbedComponentsView() = JBTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
        .also { tabPane -> tabPane.model = DefaultSingleSelectionModel() }

    private fun JTabbedPane.update(
        state: Started,
        messages: (PluginMessage) -> Unit
    ) {

        fun closeHandler(
            id: ComponentId
        ) = messages(RemoveComponent(id))

        fun addTab(
            id: ComponentId
        ) = addCloseableTab(id, ComponentView.new(this@ToolWindowView, id, component.startedStates(), state), ::closeHandler)

        state.debugState.components
            .filter { e -> indexOfTab(e.key.id) == -1 }
            .forEach { (id, _) -> addTab(id) }
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

private fun Container.first() = this[0]

private inline fun JTabbedPane.addCloseableTab(
    component: ComponentId,
    content: AwtComponent,
    crossinline onClose: (ComponentId) -> Unit
) {
    addTab(component.id, content)

    val panel = JPanel(FlowLayout()).apply {
        isOpaque = false

        add(JLabel(component.id, SwingConstants.LEADING))
        add(JLabel(CLOSE_DEFAULT_ICON).apply {
            setHover(CLOSE_DARK_ICON)
            setOnClickListener { onClose(component) }
        })
    }

    setTabComponentAt(indexOfComponent(content), panel)
}
