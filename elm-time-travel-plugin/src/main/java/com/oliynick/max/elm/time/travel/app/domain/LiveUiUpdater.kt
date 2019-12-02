package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import protocol.ComponentId

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveUiUpdater : UiUpdater {

    override fun update(
        message: UIMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is UpdatePort && state is Stopped -> updateServerSettings(state.updatedServerSettings {
                copy(
                    port = message.port
                )
            }, state)
            message is UpdateHost && state is Stopped -> updateServerSettings(state.updatedServerSettings {
                copy(
                    host = message.host
                )
            }, state)
            message === StartServer && state is Stopped -> startServer(state)
            message === StopServer && state is Started -> stopServer(state)
            message is RemoveSnapshots && state is Started -> removeSnapshots(message, state)
            message is ReApplyCommands && state is Started -> reApplyCommands(message, state)
            message is ReApplyState && state is Started -> reApplyState(message, state)
            message is RemoveComponent && state is Started -> removeComponent(message, state)
            else -> notifyIllegalMessage(message, state)
        }

    fun updateServerSettings(
        serverSettings: ServerSettings,
        state: Stopped
    ): UpdateWith<Stopped, StoreServerSettings> {
        //todo consider implementing generic memoization?
        if (state.settings.serverSettings == serverSettings) {
            return state.noCommand()
        }

        return state.update(serverSettings) command StoreServerSettings(
            serverSettings
        )
    }

    fun startServer(state: Stopped): UpdateWith<Starting, DoStartServer> =
        Starting(state.settings) command DoStartServer(
            state.settings
        )

    fun stopServer(state: Started): UpdateWith<Stopping, DoStopServer> =
        Stopping(state.settings) command DoStopServer

    fun reApplyState(
        message: ReApplyState,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoApplyState(
            message.componentId,
            message.state
        )

    fun reApplyCommands(
        message: ReApplyCommands,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoApplyCommand(
            message.componentId,
            message.command
        )

    fun removeSnapshots(
        message: RemoveSnapshots,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> {

        val component = state.debugState.components[message.componentId]
            ?: notifyUnknownComponent(message.componentId)
        val updated = component.removeSnapshots(message.ids)

        return state.updateComponents { mapping -> mapping + updated.asPair() }
            .noCommand()
    }

    fun removeComponent(
        message: RemoveComponent,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state.updateComponents { mapping -> mapping - message.componentId }
            .noCommand()

    fun notifyUnknownComponent(id: ComponentId): Nothing =
        throw IllegalArgumentException("Unknown component $id")

}
