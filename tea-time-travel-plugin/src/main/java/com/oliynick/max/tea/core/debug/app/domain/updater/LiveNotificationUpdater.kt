package com.oliynick.max.tea.core.debug.app.domain.updater

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.AppendSnapshot
import com.oliynick.max.tea.core.debug.app.domain.cms.ComponentAttached
import com.oliynick.max.tea.core.debug.app.domain.cms.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.cms.DebugState
import com.oliynick.max.tea.core.debug.app.domain.cms.DoNotifyComponentAttached
import com.oliynick.max.tea.core.debug.app.domain.cms.DoNotifyOperationException
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStopServer
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStoreServerSettings
import com.oliynick.max.tea.core.debug.app.domain.cms.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.InternalException
import com.oliynick.max.tea.core.debug.app.domain.cms.NotificationMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.NotifyOperationException
import com.oliynick.max.tea.core.debug.app.domain.cms.NotifyStarted
import com.oliynick.max.tea.core.debug.app.domain.cms.NotifyStopped
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginException
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.domain.cms.Settings
import com.oliynick.max.tea.core.debug.app.domain.cms.Snapshot
import com.oliynick.max.tea.core.debug.app.domain.cms.Started
import com.oliynick.max.tea.core.debug.app.domain.cms.StateReApplied
import com.oliynick.max.tea.core.debug.app.domain.cms.Stopped
import com.oliynick.max.tea.core.debug.app.domain.cms.Value
import com.oliynick.max.tea.core.debug.app.domain.cms.appendSnapshot
import com.oliynick.max.tea.core.debug.app.domain.cms.updateComponents
import com.oliynick.max.tea.core.debug.app.transport.StartedServer
import com.oliynick.max.tea.core.debug.app.transport.StoppedServer
import protocol.ComponentId
import java.time.LocalDateTime
import java.util.*

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveNotificationUpdater : NotificationUpdater {

    override fun update(
        message: NotificationMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is NotifyStarted -> toStartedState(message.server, state.settings)
            message is NotifyStopped -> toStoppedState(message.server, state.settings)
            message is AppendSnapshot && state is Started -> appendSnapshot(message, state)
            message is StateReApplied && state is Started -> reApplyState(message, state)
            message is ComponentAttached && state is Started -> attachComponent(message, state)
            message is NotifyOperationException -> recoverFromException(message.exception, message.operation, state)
            else -> warnUnacceptableMessage(message, state)
        }

    fun toStartedState(
        server: StartedServer,
        settings: Settings
    ): UpdateWith<Started, PluginCommand> =
        Started(
            settings,
            DebugState(),
            server
        ).noCommand()

    fun toStoppedState(
        server: StoppedServer,
        settings: Settings
    ): UpdateWith<Stopped, PluginCommand> =
        Stopped(settings, server).noCommand()

    fun appendSnapshot(
        message: AppendSnapshot,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val snapshot = message.toSnapshot()
        val updated = state.debugState.componentOrNew(message.componentId, snapshot.state)
            .appendSnapshot(snapshot)

        return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
            .noCommand()
    }

    fun attachComponent(
        message: ComponentAttached,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val id = message.componentId
        val currentState = message.state
        val componentState = state.debugState.components[id]?.copy(currentState = currentState)
            ?: ComponentDebugState(id, currentState)

        return state.updateComponents { mapping -> mapping.put(componentState.id, componentState) } command DoNotifyComponentAttached(id)
    }

    fun reApplyState(
        message: StateReApplied,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val component = state.debugState.components[message.componentId] ?: return state.noCommand()
        val updated = component.copy(currentState = message.state)

        return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
            .noCommand()
    }

    fun recoverFromException(
        th: PluginException,
        op: PluginCommand?,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        if (isFatalProblem(th, op)) notifyDeveloperException(th)
        else state command DoNotifyOperationException(th, op)

    fun warnUnacceptableMessage(
        message: PluginMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoWarnUnacceptableMessage(message, state)

    fun isFatalProblem(
        th: PluginException,
        op: PluginCommand?
    ): Boolean =
        op is DoStopServer || op is DoStoreServerSettings || th is InternalException

    fun AppendSnapshot.toSnapshot(): Snapshot =
        Snapshot(
            UUID.randomUUID(),
            LocalDateTime.now(),
            message,
            newState
        )

    fun DebugState.componentOrNew(
        id: ComponentId,
        state: Value<*>
    ) = components[id] ?: ComponentDebugState(id, state)

    fun notifyDeveloperException(cause: Throwable): Nothing =
        throw IllegalStateException(
            "Unexpected exception. Please, inform developers about the problem",
            cause
        )

}

