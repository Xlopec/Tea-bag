package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.transport.EngineManager
import com.oliynick.max.elm.time.travel.app.transport.server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

class SideToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val messages = BroadcastChannel<PluginMessage>(1)
        val deps = Dependencies(messages, EngineManager(::server))
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        suspend fun resolve(command: PluginCommand) = deps.resolve(command)

        val component = scope.component(Stopped(Settings(ServerSettings())), ::resolve, ::update, androidLogger("Plugin Component"))

        val myToolWindow = ToolWindowView(project, scope, component, messages)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.root, null, false)

        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}