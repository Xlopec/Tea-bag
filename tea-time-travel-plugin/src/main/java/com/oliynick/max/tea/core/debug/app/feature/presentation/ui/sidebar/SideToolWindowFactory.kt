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

package com.oliynick.max.tea.core.debug.app.feature.presentation.ui.sidebar

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.invoke
import com.oliynick.max.tea.core.component.states
import com.oliynick.max.tea.core.component.subscribeIn
import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.Environment
import com.oliynick.max.tea.core.debug.app.Message
import com.oliynick.max.tea.core.debug.app.PluginComponent
import com.oliynick.max.tea.core.debug.app.feature.presentation.UpdateDebugSettings
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.Plugin
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.misc.mergeWith
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.settings.PluginSettingsNotifier
import com.oliynick.max.tea.core.debug.app.feature.server.StopServer
import com.oliynick.max.tea.core.debug.app.misc.properties
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.app.state.Stopped
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SideToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val events = MutableSharedFlow<Message>()
        val environment = Environment(project.properties, project, events)
        val component = PluginComponent(environment, project.properties)

        toolWindow.contentManager.addContent(ToolWindowContent(project, component))

        environment.installResourcesDisposer(project, component)
        component.subscribeIn(events.mergeWith(project.settingsMessages), environment)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

/**
 * Awaits project close/plugin unload, after that it releases plugin resources, stops it
 */
private fun Environment.installResourcesDisposer(
    project: Project,
    component: Component<Message, State, Command>
) = launch {
    project.awaitDisposal()
    component(StopServer).takeWhile { it.currentState !is Stopped }.collect()
    cancel()
}

private suspend fun Project.awaitDisposal() =
    suspendCoroutine<Unit> {
        Disposer.register(this) { it.resume(Unit) }
    }

private val Project.settingsMessages: Flow<UpdateDebugSettings>
    get() = callbackFlow {
        val connection = messageBus.connect()

        connection.subscribe(PluginSettingsNotifier.TOPIC, object : PluginSettingsNotifier {
            override fun onSettingsUpdated(isDetailedToStringEnabled: Boolean) {
                offer(UpdateDebugSettings(isDetailedToStringEnabled))
            }
        })

        awaitClose { connection.disconnect() }
    }

private fun ToolWindowContent(
    project: Project,
    component: Component<Message, State, Command>
): Content =
    ContentFactory.SERVICE.getInstance().createContent(Plugin(project, component.states()), null, false)
