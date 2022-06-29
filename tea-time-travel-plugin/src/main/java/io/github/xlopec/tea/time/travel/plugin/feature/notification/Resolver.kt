package io.github.xlopec.tea.time.travel.plugin.feature.notification

import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.integration.NotifyCommand

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

    private fun DoNotifyComponentAttached.notifyAttached() =
        project.showBalloon(ComponentAttachedBalloon(componentId))

    private fun DoWarnUnacceptableMessage.warn() =
        project.showBalloon(UnacceptableMessageBalloon(message, state))

    private fun DoNotifyOperationException.notifyException() =
        project.showBalloon(ExceptionBalloon(exception, operation, description))
}
