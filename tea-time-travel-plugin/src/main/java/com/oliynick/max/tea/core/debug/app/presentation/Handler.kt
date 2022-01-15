package com.oliynick.max.tea.core.debug.app.presentation

import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun CoroutineScope.dispatcher(
    messages: FlowCollector<PluginMessage>,
): (PluginMessage) -> Unit =
    { message -> launch { messages.emit(message) } }