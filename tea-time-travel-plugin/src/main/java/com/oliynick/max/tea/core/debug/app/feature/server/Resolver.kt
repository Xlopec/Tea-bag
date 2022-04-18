package com.oliynick.max.tea.core.debug.app.feature.server

import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.Message
import com.oliynick.max.tea.core.debug.app.NetworkException
import com.oliynick.max.tea.core.debug.app.NotificationMessage
import com.oliynick.max.tea.core.debug.app.ServerCommand
import com.oliynick.max.tea.core.debug.app.domain.ServerAddress
import com.oliynick.max.tea.core.debug.app.feature.notification.NotifyStarted
import com.oliynick.max.tea.core.debug.app.feature.notification.NotifyStopped
import com.oliynick.max.tea.core.debug.app.feature.notification.OperationException
import com.oliynick.max.tea.core.debug.app.feature.notification.StateApplied
import com.oliynick.max.tea.core.debug.app.feature.notification.StateAppliedBalloon
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.balloon.showBalloon
import com.oliynick.max.tea.core.debug.app.toPluginException
import com.oliynick.max.tea.core.debug.protocol.ApplyMessage
import com.oliynick.max.tea.core.debug.protocol.ApplyState
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.data.mapL
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
