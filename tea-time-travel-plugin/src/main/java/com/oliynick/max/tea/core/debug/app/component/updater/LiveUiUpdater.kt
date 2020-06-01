package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.*
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.protocol.ComponentId

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveUiUpdater : UiUpdater {

    override fun update(
        message: UIMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is UpdateDebugSettings -> updateDebugSettings(message.isDetailedToStringEnabled, state)
            message is UpdateServerSettings && state is Stopped -> updateServerSettings(message, state)
            message === StartServer && state is Stopped -> startServer(state)
            message === StopServer && state is Started -> stopServer(state)
            message is RemoveSnapshots && state is Started -> removeSnapshots(message.componentId, message.ids, state)
            message is ReApplyMessage && state is Started -> reApplyCommands(message, state)
            message is ReApplyState && state is Started -> reApplyState(message, state)
            message is RemoveComponent && state is Started -> removeComponent(message, state)
            message is RemoveAllSnapshots && state is Started -> removeSnapshots(message.componentId, state)
            message is UpdateFilter && state is Started -> updateFilter(message, state)
            else -> warnUnacceptableMessage(message, state)
        }

    fun updateDebugSettings(
        isDetailedToStringEnabled: Boolean,
        state: PluginState
    ): UpdateWith<PluginState, DoStoreSettings> =
        state.updateSettings { copy(isDetailedOutput = isDetailedToStringEnabled) } command { DoStoreSettings(settings) }

    fun updateServerSettings(
        message: UpdateServerSettings,
        state: PluginState
    ): UpdateWith<PluginState, DoStoreSettings> {
        val settings = Settings.of(message.host, message.port, state.settings.isDetailedOutput)

        return state.updateServerSettings(settings) command { DoStoreSettings(settings) }
    }

    fun startServer(
        state: Stopped
    ): UpdateWith<PluginState, PluginCommand> {

        val host = state.settings.host
        val port = state.settings.port

        return if (host is Valid && port is Valid) {
            Starting(state.settings) command DoStartServer(ServerAddress(host.t, port.t), state.server)
        } else state.noCommand()
    }

    fun stopServer(
        state: Started
    ): UpdateWith<Stopping, DoStopServer> =
        Stopping(state.settings) command DoStopServer(state.server)

    fun reApplyState(
        message: ReApplyState,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoApplyState(message.componentId, state.findState(message), state.server)

    fun reApplyCommands(
        message: ReApplyMessage,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoApplyMessage(message.componentId, state.findMessage(message), state.server)

    fun removeSnapshots(
        componentId: ComponentId,
        ids: Set<SnapshotId>,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.removeSnapshots(componentId, ids).noCommand()

    fun removeSnapshots(
        componentId: ComponentId,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.removeSnapshots(componentId).noCommand()

    fun removeComponent(
        message: RemoveComponent,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.updateComponents { mapping -> mapping.remove(message.componentId) }
            .noCommand()

    fun updateFilter(
        message: UpdateFilter,
        state: Started
    ): UpdateWith<PluginState, Nothing> =
        state.updateFilter(message.id, message.input, message.ignoreCase, message.option).noCommand()

    fun Started.findState(
        message: ReApplyState
    ) = snapshot(message.componentId, message.snapshotId).state

    fun Started.findMessage(
        message: ReApplyMessage
    ) = snapshot(message.componentId, message.snapshotId).message

}
