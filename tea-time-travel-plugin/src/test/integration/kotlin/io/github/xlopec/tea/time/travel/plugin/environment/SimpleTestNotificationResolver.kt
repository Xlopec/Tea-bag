package io.github.xlopec.tea.time.travel.plugin.environment

import io.github.xlopec.tea.data.Left
import io.github.xlopec.tea.time.travel.plugin.feature.notification.DoNotifyComponentAttached
import io.github.xlopec.tea.time.travel.plugin.feature.notification.DoNotifyOperationException
import io.github.xlopec.tea.time.travel.plugin.feature.notification.DoWarnUnacceptableMessage
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotificationResolver
import io.github.xlopec.tea.time.travel.plugin.integration.NotifyCommand

class SimpleTestNotificationResolver : NotificationResolver {

    override suspend fun resolve(command: NotifyCommand): Left<Nothing?> =
        when (command) {
            is DoNotifyOperationException -> Left(null)
            is DoWarnUnacceptableMessage -> Left(null)
            is DoNotifyComponentAttached -> Left(null)
            else -> error("can't get here")
        }
}
