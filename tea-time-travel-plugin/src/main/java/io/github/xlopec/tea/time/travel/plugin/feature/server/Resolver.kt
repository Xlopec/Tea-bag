package io.github.xlopec.tea.time.travel.plugin.feature.server

import com.intellij.openapi.project.Project
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.data.mapL
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.NetworkException
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.ServerCommand
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotifyStarted
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotifyStopped
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.feature.notification.StateApplied
import io.github.xlopec.tea.time.travel.plugin.feature.notification.StateAppliedBalloon
import io.github.xlopec.tea.time.travel.plugin.feature.notification.showBalloon
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.integration.toPluginException
import io.github.xlopec.tea.time.travel.protocol.ApplyMessage
import io.github.xlopec.tea.time.travel.protocol.ApplyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

fun interface ServerCommandResolver {
    suspend fun resolveServerCommand(
        command: ServerCommand
    ): Either<NotificationMessage?, OperationException>
}

fun ServerCommandResolver(
    project: Project,
    events: MutableSharedFlow<Message>,
): ServerCommandResolver = ServerCommandResolverImpl(project, events)

private class ServerCommandResolverImpl(
    private val project: Project,
    private val events: MutableSharedFlow<Message>,
) : ServerCommandResolver {

    override suspend fun resolveServerCommand(
        command: ServerCommand
    ): Either<NotificationMessage?, OperationException> =
        when (command) {
            is DoStartServer -> command.start()
            is DoStopServer -> command.stop()
            is DoApplyMessage -> command.applyMessage()
            is DoApplyState -> command.applyState()
            else -> error("shouldn't get here")
        }

    private suspend fun DoApplyMessage.applyMessage() =
        Either(
            { server(id, ApplyMessage(command.toJsonElement())); null },
            {
                OperationException(
                    it.toPluginException(),
                    this,
                    "Plugin failed to apply message to component ${id.value}"
                )
            }
        )

    private suspend fun DoStopServer.stop() =
        Either(
            { server.stop(); NotifyStopped },
            {
                val message = "Plugin failed to stop debug server running on ${server.address.humanReadable}"
                OperationException(NetworkException(it.message ?: message, it), this, message)
            }
        )

    private suspend fun DoStartServer.start() = Either({
        withContext(Dispatchers.IO) {
            val newServer = NettyServer(address, events)
            newServer.start()
            newServer
        }
    }, {
        val message = "Plugin failed to start debug server on address ${address.humanReadable}"
        OperationException(NetworkException(it.message ?: message, it), this, message)
    }).mapL(::NotifyStarted)

    private suspend fun DoApplyState.applyState() = Either(
        {
            server(id, ApplyState(state.toJsonElement()))
            project.showBalloon(StateAppliedBalloon(id))
            StateApplied(id, state)
        }, {
            OperationException(it.toPluginException(), this, "Plugin failed to apply state to component ${id.value}")
        })
}

private val ServerAddress.humanReadable: String
    get() = "${host.value}:${port.value}"
