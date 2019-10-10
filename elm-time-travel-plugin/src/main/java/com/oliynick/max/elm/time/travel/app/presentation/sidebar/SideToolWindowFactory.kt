package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.storage.pluginSettings
import com.oliynick.max.elm.time.travel.app.storage.properties
import com.oliynick.max.elm.time.travel.app.transport.EngineManager
import com.oliynick.max.elm.time.travel.app.transport.server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SideToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val messages = Channel<PluginMessage>()
        val deps = Dependencies(messages, EngineManager(::server), project.properties)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        suspend fun resolve(command: PluginCommand) = deps.resolve(command)

        suspend fun loader() = Stopped(project.properties.pluginSettings) to emptySet<Nothing>()

        val component = scope.component(::loader, ::resolve, ::update, androidLogger("Plugin Component"))

        val myToolWindow = ToolWindowView(project, scope, component, messages)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.root, null, false)

        toolWindow.contentManager.addContent(content)

        scope.launch {

            deps.commands.asFlow().collect { command ->
                when(command) {
                    is DoNotifyMissingDependency -> Notifications.Bus.notify(command.toNotification(), project)
                }
            }
        }

    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

private fun DoNotifyMissingDependency.toNotification() = Notification("exceptions", "Missing dependency",
    "Couldn't satisfy dependencies, the error is ${exception.localizedMessage}" +
        "\nTry adding the file or directory that contains corresponding .class file",
    NotificationType.ERROR)