@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.domain.resolver

import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect
import com.oliynick.max.tea.core.debug.app.domain.cms.DoApplyCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.DoApplyState
import com.oliynick.max.tea.core.debug.app.domain.cms.DoNotifyComponentAttached
import com.oliynick.max.tea.core.debug.app.domain.cms.DoNotifyOperationException
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStartServer
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStopServer
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStoreServerSettings
import com.oliynick.max.tea.core.debug.app.domain.cms.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.NotificationMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.NotifyOperationException
import com.oliynick.max.tea.core.debug.app.domain.cms.NotifyStarted
import com.oliynick.max.tea.core.debug.app.domain.cms.NotifyStopped
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.StateReApplied
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.showBalloon
import com.oliynick.max.tea.core.debug.app.storage.serverSettings
import com.oliynick.max.tea.core.debug.app.transport.serialization.toJsonElement
import protocol.ApplyMessage
import protocol.ApplyState

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
            is DoStoreServerSettings -> command sideEffect { properties.serverSettings = serverSettings }
            is DoStartServer -> command effect { NotifyStarted(server.start(settings, events)) }
            is DoStopServer -> command effect { NotifyStopped(server.stop()) }
            is DoApplyCommand -> command sideEffect { server(id, ApplyMessage(command.command.toJsonElement())) }
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
