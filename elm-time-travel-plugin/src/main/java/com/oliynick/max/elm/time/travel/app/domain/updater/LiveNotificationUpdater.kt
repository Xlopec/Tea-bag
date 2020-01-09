package com.oliynick.max.elm.time.travel.app.domain.updater

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.app.domain.cms.*
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
            message === NotifyStarted -> toStartedState(
                state.settings)
            message === NotifyStopped -> toStoppedState(
                state.settings)
            message is AppendSnapshot && state is Started -> appendSnapshot(
                message,
                state)
            message is StateReApplied && state is Started -> reApplyState(
                message,
                state)
            message is ComponentAttached && state is Started -> attachComponent(
                message,
                state)
            message is NotifyOperationException -> recoverFromException(
                message.exception,
                message.operation,
                state
            )
            else -> notifyIllegalMessage(message, state)
        }

    fun toStartedState(settings: Settings): UpdateWith<Started, PluginCommand> =
        Started(
            settings,
            DebugState()
        ).noCommand()

    fun toStoppedState(settings: Settings): UpdateWith<Stopped, PluginCommand> =
        Stopped(settings).noCommand()

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
            ?: ComponentDebugState(
                id,
                currentState
            )

        return state.updateComponents { mapping -> mapping.put(componentState.id, componentState) }
            .noCommand()
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
    ): UpdateWith<PluginState, PluginCommand> {

        val notification by lazy {
            DoNotifyOperationException(
                th,
                op
            )
        }

        return when {
            isStartStopProblem(op) -> Stopped(state.settings).command(notification)
            isFatalProblem(th,
                           op) -> notifyDeveloperException(
                th)
            else -> state.command(notification)
        }
    }

    fun isStartStopProblem(op: PluginCommand?): Boolean =
        op === DoStopServer || op is DoStartServer

    fun isFatalProblem(
        th: PluginException,
        op: PluginCommand?
    ): Boolean =
        op is StoreFiles || op is StoreServerSettings || op is DoNotifyOperationException
            || th is InternalException

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
    ) =
        components[id] ?: ComponentDebugState(
            id,
            state
        )

    fun notifyDeveloperException(cause: Throwable): Nothing =
        throw IllegalStateException(
            "Unexpected exception. Please, inform developers about the problem",
            cause
        )

}
