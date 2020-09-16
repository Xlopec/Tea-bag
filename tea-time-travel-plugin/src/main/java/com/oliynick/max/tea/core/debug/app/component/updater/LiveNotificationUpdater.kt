package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveNotificationUpdater : NotificationUpdater {

    override fun update(
        message: NotificationMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is NotifyStarted -> toStartedState(message.server, state.settings)
            message is NotifyStopped -> toStoppedState(state.settings)
            message is AppendSnapshot && state is Started -> appendSnapshot(message, state)
            message is StateApplied && state is Started -> applyState(message, state)
            message is ComponentAttached && state is Started -> attachComponent(message, state)
            message is NotifyOperationException -> recoverFromException(message.exception, message.operation, state)
            else -> warnUnacceptableMessage(message, state)
        }

    fun toStartedState(
        server: Server,
        settings: Settings
    ): UpdateWith<Started, PluginCommand> =
        Started(
            settings,
            DebugState(),
            server
        ).noCommand()

    fun toStoppedState(
        settings: Settings
    ): UpdateWith<Stopped, PluginCommand> =
        Stopped(settings).noCommand()

    fun appendSnapshot(
        message: AppendSnapshot,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val snapshot = message.toSnapshot()
        val updated = state.debugState.componentOrNew(message.componentId, message.newState)
            .appendSnapshot(snapshot, message.newState)

        return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
            .noCommand()
    }

    fun attachComponent(
        message: ComponentAttached,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val id = message.componentId
        val currentState = message.state
        val componentState = state.debugState.components[id]?.copy(state = currentState)
            ?: ComponentDebugState(id, currentState)

        return state.updateComponents { mapping -> mapping.put(componentState.id, componentState) } command DoNotifyComponentAttached(id)
    }

    fun applyState(
        message: StateApplied,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val component = state.debugState.components[message.componentId] ?: return state.noCommand()
        val updated = component.copy(state = message.state)

        return state.updateComponents { mapping -> mapping.put(updated.id, updated) }
            .noCommand()
    }

    fun recoverFromException(
        th: PluginException,
        op: PluginCommand?,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            isFatalProblem(th, op) -> notifyDeveloperException(th)
            op is DoStartServer && state is Starting -> Stopped(state.settings) command DoNotifyOperationException(th, op)
            else -> state command DoNotifyOperationException(th, op)
        }

    fun warnUnacceptableMessage(
        message: PluginMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoWarnUnacceptableMessage(message, state)

    fun isFatalProblem(
        th: PluginException,
        op: PluginCommand?
    ): Boolean =
        op is DoStopServer || op is DoStoreSettings || th is InternalException

    fun AppendSnapshot.toSnapshot() = OriginalSnapshot(meta, message, newState)

    fun DebugState.componentOrNew(
        id: ComponentId,
        state: Value
    ) = components[id] ?: ComponentDebugState(id, state)

    fun notifyDeveloperException(cause: Throwable): Nothing =
        throw IllegalStateException(
            "Unexpected exception. Please, inform developers about the problem",
            cause
        )

}

