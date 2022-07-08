package io.github.xlopec.tea.time.travel.plugin.feature.notification

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.NotifyCommand
import io.github.xlopec.tea.time.travel.plugin.integration.PluginException
import java.util.*

fun interface NotificationResolver {
    fun resolve(
        command: NotifyCommand
    )
}

fun NotificationResolver(
    project: Project
): NotificationResolver = NotificationResolverImpl(project)

private class NotificationResolverImpl(
    private val project: Project
) : NotificationResolver {

    override fun resolve(
        command: NotifyCommand
    ) {
        when (command) {
            is DoNotifyOperationException -> command.notifyException()
            is DoWarnUnacceptableMessage -> command.warn()
            is DoNotifyComponentAttached -> command.notifyAttached()
            else -> error("can't get here")
        }
    }

    private fun DoNotifyComponentAttached.notifyAttached() {
        project.showNotification(
            "New Client Attached",
            "Component \"${componentId.value}\" attached",
            NotificationType.INFORMATION
        )
    }

    private fun DoWarnUnacceptableMessage.warn() {
        project.showNotification(
            "Tea Time Travel Plugin Exception",
            "Message ${message.javaClass.simpleName} can't be applied to state ${state.javaClass.simpleName}",
            NotificationType.WARNING
        )
    }

    private fun DoNotifyOperationException.notifyException() {
        project.showNotification(
            "Tea Time Travel Plugin Exception",
            exceptionDescription(exception, operation, description),
            NotificationType.ERROR
        )
    }
}

private fun exceptionDescription(
    cause: PluginException,
    operation: Command?,
    description: String?,
): String = "Exception occurred: ${formattedCauseDescription(cause, operation, description)}"

private fun formattedCauseDescription(
    cause: PluginException,
    operation: Command?,
    description: String?
) = (description ?: cause.message ?: operation?.javaClass?.simpleName ?: "unknown exception")
    .replaceFirstChar { it.lowercase(Locale.ENGLISH) }
