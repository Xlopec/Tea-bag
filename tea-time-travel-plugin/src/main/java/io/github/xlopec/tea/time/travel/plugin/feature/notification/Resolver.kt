package io.github.xlopec.tea.time.travel.plugin.feature.notification

import com.intellij.openapi.project.Project
import io.github.xlopec.tea.data.Left
import io.github.xlopec.tea.time.travel.plugin.NotifyCommand
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.balloon.showBalloon

fun interface NotificationResolver {
    suspend fun resolve(
        command: NotifyCommand
    ): Left<Nothing?>
}

fun NotificationResolver(
    project: Project
): NotificationResolver = NotificationResolverImpl(project)

private class NotificationResolverImpl(
    private val project: Project
) : NotificationResolver {

    override suspend fun resolve(
        command: NotifyCommand
    ): Left<Nothing?> =
        when (command) {
            is DoNotifyOperationException -> command.notifyException()
            is DoWarnUnacceptableMessage -> command.warn()
            is DoNotifyComponentAttached -> command.notifyAttached()
            else -> error("can't get here")
        }

    private fun DoNotifyComponentAttached.notifyAttached() =
        Left { project.showBalloon(ComponentAttachedBalloon(componentId)) }

    private fun DoWarnUnacceptableMessage.warn() =
        Left { project.showBalloon(UnacceptableMessageBalloon(message, state)) }

    private fun DoNotifyOperationException.notifyException() =
        Left { project.showBalloon(ExceptionBalloon(exception, operation, description)) }
}
