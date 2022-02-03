package com.oliynick.max.tea.core.debug.app.feature.notification

import com.oliynick.max.tea.core.debug.app.Command
import com.oliynick.max.tea.core.debug.app.Message
import com.oliynick.max.tea.core.debug.app.NotifyCommand
import com.oliynick.max.tea.core.debug.app.PluginException
import com.oliynick.max.tea.core.debug.app.state.State
import com.oliynick.max.tea.core.debug.protocol.ComponentId

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
