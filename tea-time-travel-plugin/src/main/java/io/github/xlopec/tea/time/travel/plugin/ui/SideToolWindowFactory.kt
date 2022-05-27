@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTeaApi::class)

package io.github.xlopec.tea.time.travel.plugin.ui

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
import io.github.xlopec.tea.core.subscribeIn
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.UpdateDebugSettings
import io.github.xlopec.tea.time.travel.plugin.feature.server.StopServer
import io.github.xlopec.tea.time.travel.plugin.feature.settings.PluginSettingsNotifier
import io.github.xlopec.tea.time.travel.plugin.integration.AppInitializer
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.Environment
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.isStarted
import io.github.xlopec.tea.time.travel.plugin.util.mergeWith
import io.github.xlopec.tea.time.travel.plugin.util.properties
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
        toolWindow: ToolWindow,
    ) {
        val events = MutableSharedFlow<Message>()
        val environment = Environment(project.properties, project, events)
        val component = PluginComponent(environment, AppInitializer(project.properties))

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
    component: Component<Message, State, Command>,
) = launch {
    project.awaitDisposal()
    component(StopServer).takeWhile { it.currentState.isStarted }.collect()
    cancel()
}

private suspend fun Project.awaitDisposal() =
    suspendCoroutine<Unit> {
        Disposer.register(this) { it.resume(Unit) }
    }

private val Project.settingsMessages: Flow<UpdateDebugSettings>
    get() = callbackFlow {
        val connection = messageBus.connect()

        connection.subscribe(
            PluginSettingsNotifier.TOPIC,
            object : PluginSettingsNotifier {
                override fun onSettingsUpdated(isDetailedToStringEnabled: Boolean) {
                    launch { send(UpdateDebugSettings(isDetailedToStringEnabled)) }
                }
            }
        )

        awaitClose { connection.disconnect() }
    }

private fun ToolWindowContent(
    project: Project,
    component: Component<Message, State, Command>,
): Content =
    ContentFactory.SERVICE.getInstance().createContent(PluginSwingAdapter(project, component.toStatesComponent()), null, false)
