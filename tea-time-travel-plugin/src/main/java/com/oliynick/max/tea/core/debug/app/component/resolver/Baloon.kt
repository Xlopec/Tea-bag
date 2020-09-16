@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.component.resolver

import com.intellij.openapi.ui.popup.Balloon
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.createErrorBalloon
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.createNotificationBalloon
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import java.util.*

fun ExceptionBalloon(
    cause: PluginException,
    operation: PluginCommand?
): Balloon =
    createErrorBalloon(htmlDescription(cause, operation))

fun StateAppliedBalloon(componentId: ComponentId): Balloon =
    createNotificationBalloon("""<html>
        <p>State was reapplied successfully for component "${componentId.value}"</p>
        </html>
    """.trimIndent())

fun ComponentAttachedBalloon(componentId: ComponentId): Balloon =
    createNotificationBalloon("""<html>
        <p>Component "${componentId.value}" attached to the plugin</p>
        </html>
    """.trimIndent())

fun UnacceptableMessageBalloon(
    message: PluginMessage,
    state: PluginState
): Balloon =
    createErrorBalloon("""<html>
    |<p>Message $message can't be applied to 
    |state $state</p>
    |</html>
""".trimMargin())

private fun htmlDescription(
    cause: PluginException,
    operation: PluginCommand?
): String {
    val message: String? = when (operation) {
        is DoStoreSettings -> "plugin failed to store settings"
        is DoStartServer -> "plugin failed to start server"
        is DoStopServer -> "plugin failed to stop server"
        is DoApplyMessage -> "plugin failed to apply commands to component \"${operation.id.value}\""
        is DoApplyState -> "plugin failed to apply state to component \"${operation.id.value}\". Check the client is running"
        null -> cause.tryGetReadableMessage()
        is DoWarnUnacceptableMessage, is DoNotifyOperationException, is DoNotifyComponentAttached -> error("will never happen")
    }

    return """<html>
        <p>An exception occurred${message?.let { ", $it" } ?: ""}</p>
        ${cause.message?.decapitalize(Locale.ENGLISH)?.let { causeMessage -> """<p>Reason: $causeMessage</p>""" }}
        </html>
    """.trimMargin()
}

private fun PluginException.tryGetReadableMessage(): String? = when (this) {
    is MissingDependenciesException -> "plugin couldn't satisfy dependencies. Try adding " +
        "the file or directory that contains corresponding .class file"
    is NetworkException -> "network exception"
    else -> null
}