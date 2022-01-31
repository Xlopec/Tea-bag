package com.oliynick.max.tea.core.debug.app.feature.server

import com.intellij.openapi.project.Project
import com.oliynick.max.entities.shared.datatypes.Either
import com.oliynick.max.entities.shared.datatypes.mapL
import com.oliynick.max.tea.core.debug.app.Message
import com.oliynick.max.tea.core.debug.app.NotificationMessage
import com.oliynick.max.tea.core.debug.app.ServerCommand
import com.oliynick.max.tea.core.debug.app.feature.notification.*
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.balloon.showBalloon
import com.oliynick.max.tea.core.debug.app.toPluginException
import com.oliynick.max.tea.core.debug.protocol.ApplyMessage
import com.oliynick.max.tea.core.debug.protocol.ApplyState
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
            { OperationException(it.toPluginException(), this, "Plugin failed to stop server") }
        )

    private suspend fun DoStartServer.start() = Either({
        withContext(Dispatchers.IO) {
            val newServer = NettyServer(address, events)
            newServer.start()
            newServer
        }
    }, {
        OperationException(it.toPluginException(), this, "Plugin failed to start server")
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


