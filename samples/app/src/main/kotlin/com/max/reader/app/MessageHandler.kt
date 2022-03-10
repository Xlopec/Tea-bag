package com.max.reader.app

import com.oliynick.max.reader.app.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

typealias MessageHandler = (Message) -> Unit

fun CoroutineScope.messageHandler(
    messages: FlowCollector<Message>,
): MessageHandler =
    { message -> launch { messages.emit(message) } }