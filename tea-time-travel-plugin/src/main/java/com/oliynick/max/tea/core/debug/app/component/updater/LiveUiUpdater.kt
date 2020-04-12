package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.app.component.cms.DoApplyMessage
import com.oliynick.max.tea.core.debug.app.component.cms.DoApplyState
import com.oliynick.max.tea.core.debug.app.component.cms.DoStartServer
import com.oliynick.max.tea.core.debug.app.component.cms.DoStopServer
import com.oliynick.max.tea.core.debug.app.component.cms.DoStoreServerSettings
import com.oliynick.max.tea.core.debug.app.component.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.component.cms.PluginState
import com.oliynick.max.tea.core.debug.app.component.cms.ReApplyMessage
import com.oliynick.max.tea.core.debug.app.component.cms.ReApplyState
import com.oliynick.max.tea.core.debug.app.component.cms.RemoveAllSnapshots
import com.oliynick.max.tea.core.debug.app.component.cms.RemoveComponent
import com.oliynick.max.tea.core.debug.app.component.cms.RemoveSnapshots
import com.oliynick.max.tea.core.debug.app.component.cms.StartServer
import com.oliynick.max.tea.core.debug.app.component.cms.Started
import com.oliynick.max.tea.core.debug.app.component.cms.Starting
import com.oliynick.max.tea.core.debug.app.component.cms.StopServer
import com.oliynick.max.tea.core.debug.app.component.cms.Stopped
import com.oliynick.max.tea.core.debug.app.component.cms.Stopping
import com.oliynick.max.tea.core.debug.app.component.cms.UIMessage
import com.oliynick.max.tea.core.debug.app.component.cms.UpdateFilter
import com.oliynick.max.tea.core.debug.app.component.cms.UpdateHost
import com.oliynick.max.tea.core.debug.app.component.cms.UpdatePort
import com.oliynick.max.tea.core.debug.app.component.cms.removeSnapshots
import com.oliynick.max.tea.core.debug.app.component.cms.snapshot
import com.oliynick.max.tea.core.debug.app.component.cms.update
import com.oliynick.max.tea.core.debug.app.component.cms.updateComponents
import com.oliynick.max.tea.core.debug.app.component.cms.updateFilter
import com.oliynick.max.tea.core.debug.app.component.cms.updatedServerSettings
import com.oliynick.max.tea.core.debug.app.domain.ServerSettings
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import protocol.ComponentId

// privacy is for pussies
@Suppress("MemberVisibilityCanBePrivate")
object LiveUiUpdater : UiUpdater {

    override fun update(
        message: UIMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when {
            message is UpdatePort && state is Stopped -> updateServerSettings(
                // fixme чому ЇЇЇЇЇЇЇїїїїїїї??777(((99
                state.updatedServerSettings {
                    copy(
                        port = message.port
                    )
                },
                state
            )
            message is UpdateHost && state is Stopped -> updateServerSettings(
                state.updatedServerSettings {
                    copy(
                        host = message.host
                    )
                },
                state
            )
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

    fun updateServerSettings(
        serverSettings: ServerSettings,
        state: Stopped
    ): UpdateWith<Stopped, DoStoreServerSettings> {
        //todo consider implementing generic memoization?
        if (state.settings.serverSettings == serverSettings) {
            return state.noCommand()
        }

        return state.update(serverSettings) command DoStoreServerSettings(serverSettings)
    }

    fun startServer(state: Stopped): UpdateWith<Starting, DoStartServer> =
        Starting(state.settings) command DoStartServer(state.settings, state.server)

    fun stopServer(state: Started): UpdateWith<Stopping, DoStopServer> =
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
    ): UpdateWith<PluginState, PluginCommand> =
        state.removeSnapshots(componentId, ids).noCommand()

    fun removeSnapshots(
        componentId: ComponentId,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state.removeSnapshots(componentId).noCommand()

    fun removeComponent(
        message: RemoveComponent,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state.updateComponents { mapping -> mapping.remove(message.componentId) }
            .noCommand()

    fun updateFilter(
        message: UpdateFilter,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state.updateFilter(message.id, message.input, message.ignoreCase, message.option).noCommand()

    fun Started.findState(
        message: ReApplyState
    ) = snapshot(message.componentId, message.snapshotId).state

    fun Started.findMessage(
        message: ReApplyMessage
    ) = snapshot(message.componentId, message.snapshotId).message

}
