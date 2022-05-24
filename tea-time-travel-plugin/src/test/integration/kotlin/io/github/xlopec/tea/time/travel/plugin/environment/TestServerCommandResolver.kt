package io.github.xlopec.tea.time.travel.plugin.environment

import androidx.compose.ui.test.IdlingResource
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.data.Left
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotifyStarted
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotifyStopped
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.feature.notification.StateApplied
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoApplyState
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoStopServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.ServerCommandResolver
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.ServerCommand

interface TestServerCommandResolver : ServerCommandResolver, IdlingResource
class SimpleTestServerCommandResolver : TestServerCommandResolver {
    override suspend fun resolveServerCommand(command: ServerCommand): Either<NotificationMessage?, OperationException> =
        when (command) {
            is DoStartServer -> Left(NotifyStarted(StartedTestServerStub))
            is DoStopServer -> Left(NotifyStopped)
            is DoApplyMessage -> Left(null)
            is DoApplyState -> Left(StateApplied(command.id, command.state))
            else -> error("shouldn't get here")
        }

    override val isIdleNow: Boolean = true
}
