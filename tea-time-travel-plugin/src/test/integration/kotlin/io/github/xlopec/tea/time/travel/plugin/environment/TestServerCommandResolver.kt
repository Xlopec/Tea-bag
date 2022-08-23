package io.github.xlopec.tea.time.travel.plugin.environment

import androidx.compose.ui.test.IdlingResource
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ServerStarted
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ServerStopped
import io.github.xlopec.tea.time.travel.plugin.feature.notification.StateDeployed
import io.github.xlopec.tea.time.travel.plugin.feature.server.*
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.ServerCommand

interface TestServerCommandResolver : ServerCommandResolver, IdlingResource
class SimpleTestServerCommandResolver : TestServerCommandResolver {
    override fun resolveServerCommand(
        command: ServerCommand,
        ctx: ResolveCtx<Message>,
    ) {
        ctx.effect {
            when (command) {
                is DoStartServer -> ServerStarted(StartedTestServerStub)
                is DoStopServer -> ServerStopped
                is DoApplyMessage -> null
                is DoApplyState -> StateDeployed(command.id, command.state)
                else -> error("shouldn't get here")
            }
        }
    }

    override val isIdleNow: Boolean = true
}
