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

package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBTabbedPane
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.changes
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.component.ComponentView
import com.oliynick.max.elm.time.travel.app.presentation.misc.setHover
import com.oliynick.max.elm.time.travel.app.presentation.info.InfoView
import com.oliynick.max.elm.time.travel.app.presentation.misc.DefaultDocumentListener
import com.oliynick.max.elm.time.travel.app.presentation.misc.removeMouseListeners
import com.oliynick.max.elm.time.travel.app.presentation.misc.safe
import com.oliynick.max.elm.time.travel.app.presentation.misc.setOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protocol.ComponentId
import java.awt.Container
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.DocumentListener
import java.awt.Component as AwtComponent

class ToolWindowView(private val project: Project,
                     private val scope: CoroutineScope,
                     private val component: Component<PluginMessage, PluginState>,
                     private val uiEvents: Channel<PluginMessage>) : CoroutineScope by scope {

    private lateinit var panel: JPanel
    private lateinit var startButton: JLabel
    private lateinit var portTextField: JTextField
    private lateinit var hostTextField: JTextField
    private lateinit var componentsPanel: JPanel

    private val portListener = object : DefaultDocumentListener {
        override fun onValueUpdated(value: String) {
            uiEvents.offer(UpdatePort(value.toUIntOrNull() ?: return))
        }
    }

    private val hostListener = object : DefaultDocumentListener {
        override fun onValueUpdated(value: String) {
            uiEvents.offer(UpdateHost(value))
        }
    }

    val root: JPanel get() = panel

    init {
        launch { component(uiEvents.consumeAsFlow()).collect { state -> render(state, uiEvents) } }
        launch { component.showProgressOnTransientState(project) }
    }

    //todo consider exposing a single callback
    private fun render(state: PluginState, messages: Channel<PluginMessage>) {

        portTextField.isEnabled = state is Stopped
        hostTextField.isEnabled = portTextField.isEnabled

        when (state) {
            is Stopped -> render(state, messages)
            is Starting -> render(state)
            is Started -> render(state, messages)
            is Stopping -> render(state)
        }.safe
    }

    private fun render(state: Stopped, messages: Channel<PluginMessage>) {
        portTextField.setText(state.settings.serverSettings.port.toString(), portListener)
        hostTextField.setText(state.settings.serverSettings.host, hostListener)

        startButton.icon = getIcon("run")
        startButton.disabledIcon = getIcon("run_disabled")

        startButton.setOnClickListenerEnabling { messages.offer(StartServer) }

        val shouldRemoveOrEmpty = componentsPanel.isEmpty || (componentsPanel.isNotEmpty && componentsPanel.first().name != InfoView.NAME)

        if (shouldRemoveOrEmpty) {
            showEmptyComponentsView()
        }

        check(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children()}" }
    }

    private fun render(@Suppress("UNUSED_PARAMETER") state: Starting) {
        startButton.icon = getIcon("run")
        startButton.disabledIcon = getIcon("resume")

        startButton.removeMouseListenersDisabling()
    }

    private fun render(state: Started, messages: Channel<PluginMessage>) {
        startButton.icon = getIcon("suspend")
        startButton.disabledIcon = getIcon("suspend_disabled")

        startButton.setOnClickListenerEnabling { messages.offer(StopServer) }

        require(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children()}" }

        if (state.debugState.components.isEmpty()) {
            // show empty view
            if (componentsPanel.first().name != InfoView.NAME) {
                showEmptyComponentsView()
            }
        } else {

            if (componentsPanel.first().name == InfoView.NAME) {
                // swap panels
                componentsPanel.clearCancelling()
                componentsPanel += tabbedComponentsView()
            }

            (componentsPanel.first() as JTabbedPane).update(state.debugState, messages)
        }

        check(componentsPanel.componentCount == 1) { "Invalid components count, children ${componentsPanel.children()}" }
    }

    private fun render(@Suppress("UNUSED_PARAMETER") state: Stopping) {
        startButton.icon = getIcon("suspend")
        startButton.disabledIcon = getIcon("killProcess")

        startButton.removeMouseListenersDisabling()
    }

    private fun showEmptyComponentsView() {
        componentsPanel.clearCancelling()
        componentsPanel += InfoView(component, scope.coroutineContext).root
    }

    private fun tabbedComponentsView() = JBTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
        .also { tabPane -> tabPane.model = DefaultSingleSelectionModel() }

    private fun JTabbedPane.update(debugState: DebugState, messages: Channel<PluginMessage>) {
        debugState.components
            .filter { e -> indexOfTab(e.key.id) == -1 }
            .forEach { (id, s) ->
                addCloseableTab(id, ComponentView(scope, component, s)._root) { component ->
                    messages.offer(RemoveComponent(component))
                }
            }
    }

}

private operator fun Container.plusAssign(component: AwtComponent) {
    add(component)
}

private fun JTextField.setText(text: String, listener: DocumentListener) {
    document.removeDocumentListener(listener)
    this.text = text
    document.addDocumentListener(listener)
}

private suspend fun Component<PluginMessage, PluginState>.showProgressOnTransientState(project: Project) {
    changes().filter { state -> state.isTransient }.collect { transientState ->
        runBackgroundableTask(transientState.progressDescription, project, false) {
            runBlocking { changes().first { state -> !state.isTransient } }
        }
    }
}

private val PluginState.isTransient inline get() = this is Starting || this is Stopping

private val ServerSettings.asHumanReadable: String inline get() = "$host:$port"

private val PluginState.progressDescription: String
    inline get() = when (this) {
        is Starting -> "Starting server on ${settings.serverSettings.asHumanReadable}"
        is Stopping -> "Stopping server on ${settings.serverSettings.asHumanReadable}"
        is Stopped, is Started -> throw IllegalStateException("Can't get description for $this")
    }

private fun AwtComponent.setOnClickListenerEnabling(l: (MouseEvent) -> Unit) {
    setOnClickListener(l)
    isEnabled = true
}

private fun AwtComponent.removeMouseListenersDisabling() {
    removeMouseListeners()
    isEnabled = false
}

inline val Container.isEmpty inline get() = componentCount == 0

inline val Container.isNotEmpty inline get() = !isEmpty

fun Container.first() = this[0]

operator fun Container.get(i: Int): AwtComponent = getComponent(i)

fun Container.clearCancelling() {
    for (i in 0 until componentCount) {
        val c = getComponent(i)

        if (c is CoroutineScope) {
            c.cancel()
        }

        remove(c)
    }
}

fun AwtComponent.cancel() {
    if (this is CoroutineScope) {
        // fixme, with CoroutineScope.cancel() it doesn't even compile
        coroutineContext[Job.Key]?.cancel()
    }
}

fun Container.children() = (0 until componentCount).map { i -> this[i] }

inline fun JTabbedPane.addCloseableTab(component: ComponentId,
                                       content: AwtComponent,
                                       icon: Icon? = null,
                                       crossinline onClose:(ComponentId) -> Unit) {
    addTab(component.id, content)

    val panel = JPanel(FlowLayout()).apply {
        isOpaque = false

        add(JLabel(component.id, icon, SwingConstants.LEADING))
        add(JLabel(getIcon("close")).apply {
            setHover(getIcon("close_dark"))
            setOnClickListener { onClose(component) }
        })
    }

    setTabComponentAt(indexOfComponent(content), panel)
}

fun getIcon(name: String): Icon {
    return IconLoader.getIcon("images/$name.png")
}