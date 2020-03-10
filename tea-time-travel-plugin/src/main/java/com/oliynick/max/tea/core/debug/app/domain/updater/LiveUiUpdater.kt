package com.oliynick.max.tea.core.debug.app.domain.updater

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.DoApplyCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.DoApplyState
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStartServer
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStopServer
import com.oliynick.max.tea.core.debug.app.domain.cms.DoStoreServerSettings
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.domain.cms.ReApplyCommands
import com.oliynick.max.tea.core.debug.app.domain.cms.ReApplyState
import com.oliynick.max.tea.core.debug.app.domain.cms.RemoveAllSnapshots
import com.oliynick.max.tea.core.debug.app.domain.cms.RemoveComponent
import com.oliynick.max.tea.core.debug.app.domain.cms.RemoveSnapshots
import com.oliynick.max.tea.core.debug.app.domain.cms.ServerSettings
import com.oliynick.max.tea.core.debug.app.domain.cms.StartServer
import com.oliynick.max.tea.core.debug.app.domain.cms.Started
import com.oliynick.max.tea.core.debug.app.domain.cms.Starting
import com.oliynick.max.tea.core.debug.app.domain.cms.StopServer
import com.oliynick.max.tea.core.debug.app.domain.cms.Stopped
import com.oliynick.max.tea.core.debug.app.domain.cms.Stopping
import com.oliynick.max.tea.core.debug.app.domain.cms.UIMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.UpdateHost
import com.oliynick.max.tea.core.debug.app.domain.cms.UpdatePort
import com.oliynick.max.tea.core.debug.app.domain.cms.removeSnapshots
import com.oliynick.max.tea.core.debug.app.domain.cms.update
import com.oliynick.max.tea.core.debug.app.domain.cms.updateComponents
import com.oliynick.max.tea.core.debug.app.domain.cms.updatedServerSettings
import protocol.ComponentId
import java.util.*

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
                state)
            message is UpdateHost && state is Stopped -> updateServerSettings(
                state.updatedServerSettings {
                    copy(
                        host = message.host
                    )
                },
                state)
            message === StartServer && state is Stopped -> startServer(state)
            message === StopServer && state is Started -> stopServer(state)
            message is RemoveSnapshots && state is Started -> removeSnapshots(message.componentId, message.ids, state)
            message is ReApplyCommands && state is Started -> reApplyCommands(message, state)
            message is ReApplyState && state is Started -> reApplyState(message, state)
            message is RemoveComponent && state is Started -> removeComponent(message, state)
            message is RemoveAllSnapshots && state is Started -> removeSnapshots(message.componentId, state)
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
        state command DoApplyState(message.componentId, message.state, state.server)

    fun reApplyCommands(
        message: ReApplyCommands,
        state: Started
    ): UpdateWith<PluginState, PluginCommand> =
        state command DoApplyCommand(message.componentId, message.command, state.server)

    fun removeSnapshots(
        componentId: ComponentId,
        ids: Set<UUID>,
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

}
