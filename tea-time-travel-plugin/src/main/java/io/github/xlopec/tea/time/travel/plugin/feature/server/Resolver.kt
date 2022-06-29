package io.github.xlopec.tea.time.travel.plugin.feature.server

import com.intellij.openapi.project.Project
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.time.travel.plugin.feature.notification.*
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.NetworkException
import io.github.xlopec.tea.time.travel.plugin.integration.ServerCommand
import io.github.xlopec.tea.time.travel.plugin.integration.toPluginException
import io.github.xlopec.tea.time.travel.protocol.ApplyMessage
import io.github.xlopec.tea.time.travel.protocol.ApplyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

fun interface ServerCommandResolver {
    fun resolveServerCommand(
        command: ServerCommand,
        ctx: ResolveCtx<Message>,
    )
}

fun ServerCommandResolver(
    project: Project,
    events: MutableSharedFlow<Message>,
): ServerCommandResolver = ServerCommandResolverImpl(project, events)

private class ServerCommandResolverImpl(
    private val project: Project,
    private val events: MutableSharedFlow<Message>,
) : ServerCommandResolver {

    override fun resolveServerCommand(
        command: ServerCommand,
        ctx: ResolveCtx<Message>,
    ) {
        ctx.effects {
            when (command) {
                is DoStartServer -> command.start()
                is DoStopServer -> command.stop()
                is DoApplyMessage -> command.applyMessage()
                is DoApplyState -> command.applyState()
                else -> error("shouldn't get here")
            }.fold(::setOf, ::setOfNotNull)
        }
    }

    private suspend fun DoApplyMessage.applyMessage() =
        arrow.core.Either.catch { server(id, ApplyMessage(command.toJsonElement())); null }
            .mapLeft {
                OperationException(
                    it.toPluginException(),
                    this,
                    "Plugin failed to apply message to component ${id.value}"
                )
            }

    private suspend fun DoStopServer.stop() =
        arrow.core.Either.catch { server.stop() }
            .mapLeft {
                val message = "Plugin failed to stop debug server running on ${server.address.humanReadable}"
                OperationException(NetworkException(it.message ?: message, it), this, message)
            }.map { NotifyStopped }

    private suspend fun DoStartServer.start() =
        arrow.core.Either.catch {
            withContext(Dispatchers.IO) {
                val newServer = NettyServer(address, events)
                newServer.start()
                newServer
            }
        }.mapLeft {
            val message =
                "Plugin failed to start debug server on address ${address.humanReadable}"
            OperationException(NetworkException(it.message ?: message, it), this, message)
        }.map(::NotifyStarted)

    private suspend fun DoApplyState.applyState() = arrow.core.Either.catch {
        server(id, ApplyState(state.toJsonElement()))
        project.showBalloon(StateAppliedBalloon(id))
        StateApplied(id, state)
    }.mapLeft {
        OperationException(
            it.toPluginException(),
            this,
            "Plugin failed to apply state to component ${id.value}"
        )
    }
}

private val ServerAddress.humanReadable: String
    get() = "${host.value}:${port.value}"
