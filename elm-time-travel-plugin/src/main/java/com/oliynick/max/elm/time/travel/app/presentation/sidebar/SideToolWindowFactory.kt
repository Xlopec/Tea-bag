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

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.oliynick.max.elm.core.actor.component
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.storage.pluginSettings
import com.oliynick.max.elm.time.travel.app.storage.properties
import com.oliynick.max.elm.time.travel.app.transport.ServerHandler
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
        val dependencies = Dependencies(messages, ServerHandler(), project.properties)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        suspend fun resolve(command: PluginCommand) = dependencies.resolve(command)

        suspend fun loader() = Stopped(project.properties.pluginSettings) to emptySet<Nothing>()

        val component = scope.component(::loader, ::resolve, ::update, androidLogger("Plugin Component"))

        val myToolWindow = ToolWindowView(project, scope, component, messages)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.root, null, false)

        toolWindow.contentManager.addContent(content)

        scope.launch {
            dependencies.exceptions.asFlow().collect { command -> project.showException(command) }
        }
        scope.launch {
            dependencies.notifications.asFlow().collect { notification -> project.showNotification(notification) }
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

private fun Project.showException(command: DoNotifyOperationException) {
    showBalloon(createBalloon(command.exception, command.operation))
}

private fun Project.showNotification(notification: NotificationMessage) {
    val balloon: Balloon = when(notification) {
        NotifyStarted -> createServerStartedBalloon()
        NotifyStopped -> createServerStoppedBalloon()
        is StateReApplied -> createStateReAppliedBalloon(notification.componentId)
        is ComponentAttached -> createComponentAttachedBalloon(notification.componentId)
        else -> return
    }

    showBalloon(balloon)
}