package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import java.awt.Color
import java.awt.Insets
import javax.swing.Icon

private val exceptionBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
private val exceptionBalloonFillColor = JBColor(0xffcccc, 0x704745)
private val exceptionBalloonIcon by lazy { AllIcons.General.NotificationError!! }

private val notificationBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
private val notificationBalloonFillColor = JBColor(0xbaeeba, 0x33412E)
private val notificationBalloonIcon by lazy { AllIcons.General.NotificationInfo!! }

fun Project.showBalloon(balloon: Balloon) {
    val point = RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(this).component)

    balloon.show(point, Balloon.Position.atRight)
}

fun createBalloon(cause: PluginException, operation: PluginCommand?): Balloon {
    return createBalloon(htmlDescription(cause, operation), exceptionBalloonIcon, exceptionBalloonTextColor, exceptionBalloonFillColor)
}

fun createServerStartedBalloon(): Balloon {
    return createNotificationBalloon("""<html>
        <p>Server started successfully</p>
        </html>
    """.trimIndent())
}

fun createServerStoppedBalloon(): Balloon {
    return createNotificationBalloon("""<html>
        <p>Server stopped successfully</p>
        </html>
    """.trimIndent())
}

fun createStateReAppliedBalloon(componentId: ComponentId): Balloon {
    return createNotificationBalloon("""<html>
        <p>State was reapplied successfully for component "${componentId.id}"</p>
        </html>
    """.trimIndent())
}

fun createComponentAttachedBalloon(componentId: ComponentId): Balloon {
    return createNotificationBalloon("""<html>
        <p>Component "${componentId.id}" attached to the plugin</p>
        </html>
    """.trimIndent())
}

private fun createNotificationBalloon(html: String): Balloon {
    return createBalloon(html, notificationBalloonIcon, notificationBalloonTextColor, notificationBalloonFillColor)
}

private fun createBalloon(html: String, icon: Icon, textColor: JBColor, fillColor: JBColor): Balloon {
    return JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(html, icon, textColor, fillColor, null)
        .setBlockClicksThroughBalloon(true)
        .setFadeoutTime(15_000L)
        .setContentInsets(Insets(15, 15, 15, 15))
        .setCornerToPointerDistance(30)
        .setCloseButtonEnabled(true)
        .createBalloon()
}

private fun htmlDescription(cause: PluginException, operation: PluginCommand?): String {
    val message: String? = when (operation) {
        is StoreFiles, is StoreServerSettings, is DoNotifyOperationException -> TODO()
        is DoStartServer -> "plugin failed to start server"
        DoStopServer -> "plugin failed to stop server"
        is DoApplyCommands -> "plugin failed to apply commands to component \"${operation.id.id}\""
        is DoApplyState -> "plugin failed to apply state to component \"${operation.id.id}\". Check the client is running"
        null -> cause.tryGetReadableMessage()
    }

    return """<html>
        <p>An exception occurred${message?.let { ", $it" } ?: ""}</p>
        <p>Reason: ${cause.message}</p>
        </html>
    """.trimMargin()
}

private fun PluginException.tryGetReadableMessage(): String? = when(this) {
    is MissingDependenciesException -> "plugin couldn't satisfy dependencies. Try adding " +
            "the file or directory that contains corresponding .class file"
    is NetworkException -> "network exception"
    else -> null
}