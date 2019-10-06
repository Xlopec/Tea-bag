package com.oliynick.max.elm.time.travel.app

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.time.travel.app.plugin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel

class ElmToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val uiEvents = Channel<PluginMessage>()
        val settings = Settings(ServerSettings())
        val deps = Dependencies(uiEvents, EngineManager(::server))

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        suspend fun resolve(command: PluginCommand) = deps.resolve(command)

        val component = scope.component(Stopped(settings), ::resolve, ::update, androidLogger("Plugin Component"))

        val myToolWindow = ToolWindowView(project, scope, component, uiEvents)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.root, null, false)

        toolWindow.contentManager.addContent(content)
    }

}