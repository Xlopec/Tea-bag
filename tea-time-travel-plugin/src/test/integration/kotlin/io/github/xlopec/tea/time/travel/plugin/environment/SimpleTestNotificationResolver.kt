package io.github.xlopec.tea.time.travel.plugin.environment

import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotificationResolver
import io.github.xlopec.tea.time.travel.plugin.integration.NotifyCommand

class SimpleTestNotificationResolver : NotificationResolver {
    override fun resolve(command: NotifyCommand) = Unit
}
