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

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
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
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import java.awt.Color
import java.awt.Insets


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
            dependencies.exceptions.asFlow().collect { command ->
                project.showBalloon(createBalloon(command.exception, command.operation))
            }
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

private val exceptionBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
private val exceptionBalloonFillColor = JBColor(0xffcccc, 0x704745)
private val exceptionBalloonIcon by lazy { AllIcons.General.NotificationError!! }

fun Project.showBalloon(balloon: Balloon) {
    val point = RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(this).component)

    balloon.show(point, Balloon.Position.atRight)
}

fun createBalloon(cause: Throwable, operation: PluginCommand?): Balloon {
    return JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(htmlDescription(cause, operation), exceptionBalloonIcon, exceptionBalloonTextColor, exceptionBalloonFillColor, null)
        .setBlockClicksThroughBalloon(true)
        .setFadeoutTime(20_000L)
        .setContentInsets(Insets(15, 15, 15, 15))
        .setCornerToPointerDistance(30)
        .setShowCallout(true)
        .setCloseButtonEnabled(true)
        .createBalloon()
}

private fun htmlDescription(cause: Throwable, operation: PluginCommand?): String {
    val message: String? = when (operation) {
        is StoreFiles, is StoreServerSettings, is DoNotifyOperationException -> TODO()
        is DoStartServer -> "plugin failed to start server"
        DoStopServer -> "plugin failed to stop server"
        is DoApplyCommands -> "plugin failed to apply commands to component \"${operation.id.id}\""
        is DoApplyState -> "plugin failed to apply state to component \"${operation.id.id}\". Check the client is running"
        null -> cause.tryGetReadableMessage()
    }

    cause.printStackTrace()

    return """<html>
        <p>An exception occurred${message?.let { ", $it" } ?: ""}</p>
        <p>Reason: ${cause.message}</p>
        </html>
    """.trimMargin()
}

private fun Throwable.tryGetReadableMessage(): String? = when {
    isMissingDependenciesException -> "plugin couldn't satisfy dependencies. Try adding " +
            "the file or directory that contains corresponding .class file"
    isNetworkException -> "network exception"
    else -> null
}