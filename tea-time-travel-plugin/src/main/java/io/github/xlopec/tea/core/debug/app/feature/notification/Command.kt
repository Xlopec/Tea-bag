package io.github.xlopec.tea.core.debug.app.feature.notification

import io.github.xlopec.tea.core.debug.app.Command
import io.github.xlopec.tea.core.debug.app.Message
import io.github.xlopec.tea.core.debug.app.NotifyCommand
import io.github.xlopec.tea.core.debug.app.PluginException
import io.github.xlopec.tea.core.debug.app.state.State
import io.github.xlopec.tea.core.debug.protocol.ComponentId

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
