@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.component.resolver

import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.presentation.ui.balloon.showBalloon
import com.oliynick.max.tea.core.debug.app.misc.settings
import com.oliynick.max.tea.core.debug.app.transport.serialization.toJsonElement
import com.oliynick.max.tea.core.debug.protocol.ApplyMessage
import com.oliynick.max.tea.core.debug.protocol.ApplyState

fun <Env> LiveAppResolver() where Env : HasMessageChannel,
                                  Env : HasProject,
                                  Env : HasSystemProperties,
                                  Env : HasServer = object : LiveAppResolver<Env> {}

interface LiveAppResolver<Env> : AppResolver<Env> where Env : HasMessageChannel,
                                                        Env : HasProject,
                                                        Env : HasSystemProperties,
                                                        Env : HasServer {

    override suspend fun Env.resolve(
        command: PluginCommand
    ): Set<PluginMessage> =
        runCatching { doResolve(command) }
            .getOrElse { th -> setOf(NotifyOperationException(th, command)) }

    suspend fun Env.doResolve(
        command: PluginCommand
    ): Set<NotificationMessage> =
        when (command) {
            is DoStoreSettings -> command sideEffect { properties.settings = settings }
            is DoStartServer -> command effect { NotifyStarted(newServer(address, events)) }
            is DoStopServer -> command effect { server.stop(); NotifyStopped }
            is DoApplyMessage -> command sideEffect { server(id, ApplyMessage(command.command.toJsonElement())) }
            is DoApplyState -> reApplyState(command)
            is DoNotifyOperationException -> command sideEffect { project.showBalloon(ExceptionBalloon(exception, operation)) }
            is DoWarnUnacceptableMessage -> command sideEffect { project.showBalloon(UnacceptableMessageBalloon(message, state)) }
            is DoNotifyComponentAttached -> command sideEffect { project.showBalloon(ComponentAttachedBalloon(componentId)) }
        }

    suspend fun Env.reApplyState(
        command: DoApplyState
    ) = command effect {
        server(id, ApplyState(state.toJsonElement()))
        project.showBalloon(StateAppliedBalloon(id))
        StateApplied(id, state)
    }

}
