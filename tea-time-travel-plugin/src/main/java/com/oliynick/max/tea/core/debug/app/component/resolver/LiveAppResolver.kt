@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.component.resolver

import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.showBalloon
import com.oliynick.max.tea.core.debug.app.storage.pluginSettings
import com.oliynick.max.tea.core.debug.app.transport.serialization.toJsonElement
import com.oliynick.max.tea.core.debug.protocol.ApplyMessage
import com.oliynick.max.tea.core.debug.protocol.ApplyState

fun <Env> LiveAppResolver() where Env : HasMessageChannel,
                                  Env : HasProject,
                                  Env : HasSystemProperties = object : LiveAppResolver<Env> {}

interface LiveAppResolver<Env> : AppResolver<Env> where Env : HasMessageChannel,
                                                        Env : HasProject,
                                                        Env : HasSystemProperties {

    override suspend fun Env.resolve(
        command: PluginCommand
    ): Set<PluginMessage> =
        runCatching { doResolve(command) }
            .getOrElse { th -> setOf(NotifyOperationException(th, command)) }

    suspend fun Env.doResolve(
        command: PluginCommand
    ): Set<NotificationMessage> =
        when (command) {
            is DoStoreSettings -> command sideEffect { properties.pluginSettings = settings }
            is DoStartServer -> command effect { NotifyStarted(server.start(settings, events)) }
            is DoStopServer -> command effect { NotifyStopped(server.stop()) }
            is DoApplyMessage -> command sideEffect {
                server(
                        id,
                        ApplyMessage(command.command.toJsonElement())
                )
            }
            is DoApplyState -> reApplyState(command)
            is DoNotifyOperationException -> command sideEffect { project.showBalloon(newExceptionBalloon(exception, operation)) }
            is DoWarnUnacceptableMessage -> command sideEffect { project.showBalloon(newUnacceptableMessageBalloon(message, state)) }
            is DoNotifyComponentAttached -> command sideEffect { project.showBalloon(newComponentAttachedBalloon(componentId)) }
        }

    suspend fun Env.reApplyState(
        command: DoApplyState
    ) = command effect {
        server(id, ApplyState(state.toJsonElement()))
        project.showBalloon(newStateReAppliedBalloon(id))
        StateReApplied(id, state)
    }

}
