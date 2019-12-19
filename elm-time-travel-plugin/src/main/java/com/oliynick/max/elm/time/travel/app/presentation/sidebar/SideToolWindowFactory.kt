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
import com.oliynick.max.elm.time.travel.app.env.Environment
import com.oliynick.max.elm.time.travel.app.env.PluginComponent
import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.app.storage.properties
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class SideToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        Environment(project.properties).createToolWindowContent(project, toolWindow)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

private fun Environment.createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val myToolWindow = ToolWindowView(project, this, PluginComponent(), channels.events)
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val content = contentFactory.createContent(myToolWindow.root, null, false)

    toolWindow.contentManager.addContent(content)

    launch {
        channels.exceptions.asFlow().collect { command ->
            project.showException(command)
        }
    }

    launch {
        channels.notifications.asFlow().collect { notification ->
            project.showNotification(notification)
        }
    }
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