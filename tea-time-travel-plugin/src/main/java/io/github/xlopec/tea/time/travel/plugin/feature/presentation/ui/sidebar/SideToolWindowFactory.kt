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

package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.sidebar

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.core.subscribeIn
import io.github.xlopec.tea.time.travel.plugin.Command
import io.github.xlopec.tea.time.travel.plugin.Environment
import io.github.xlopec.tea.time.travel.plugin.Message
import io.github.xlopec.tea.time.travel.plugin.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.UpdateDebugSettings
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.mergeWith
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.settings.PluginSettingsNotifier
import io.github.xlopec.tea.time.travel.plugin.feature.server.StopServer
import io.github.xlopec.tea.time.travel.plugin.misc.properties
import io.github.xlopec.tea.time.travel.plugin.model.state.State
import io.github.xlopec.tea.time.travel.plugin.model.state.Stopped
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class SideToolWindowFactory : ToolWindowFactory, DumbAware {

    @OptIn(ExperimentalTeaApi::class)
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
    ContentFactory.SERVICE.getInstance().createContent(Plugin(project, component.toStatesComponent()), null, false)
