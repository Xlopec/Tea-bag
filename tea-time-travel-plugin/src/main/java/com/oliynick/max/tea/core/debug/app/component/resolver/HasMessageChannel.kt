@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.component.resolver

import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import kotlinx.coroutines.channels.BroadcastChannel

fun HasMessagesChannel(
    events: BroadcastChannel<PluginMessage> = BroadcastChannel(1)
) = object : HasMessageChannel {
    override val events = events
}

interface HasMessageChannel {
    val events: BroadcastChannel<PluginMessage>
}