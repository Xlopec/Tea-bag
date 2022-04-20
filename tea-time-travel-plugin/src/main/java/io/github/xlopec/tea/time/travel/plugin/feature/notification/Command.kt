package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.time.travel.plugin.Command
import io.github.xlopec.tea.time.travel.plugin.Message
import io.github.xlopec.tea.time.travel.plugin.NotifyCommand
import io.github.xlopec.tea.time.travel.plugin.PluginException
import io.github.xlopec.tea.time.travel.plugin.state.State
import io.github.xlopec.tea.time.travel.protocol.ComponentId

data class DoNotifyOperationException(
    val exception: PluginException,
    val operation: Command?,
    val description: String?
) : NotifyCommand

data class DoWarnUnacceptableMessage(
    val message: Message,
    val state: State
) : NotifyCommand

@JvmInline
value class DoNotifyComponentAttached(
    val componentId: ComponentId
) : NotifyCommand
