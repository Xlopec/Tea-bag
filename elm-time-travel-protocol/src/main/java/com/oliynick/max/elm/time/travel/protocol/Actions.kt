package com.oliynick.max.elm.time.travel.protocol

sealed class Message

data class ApplyMessage(val message: Any) : Message()

data class NotifyComponentSnapshot(val message: Any, val oldState: Any, val newState: Any) : Message()

data class NotifyStateUpdated(val newState: Any) : Message()

data class ApplyState(val state: Any) : Message()

fun notifyUnexpectedMessage(message: Message): Nothing {
    throw IllegalArgumentException("Received illegal action, was $message")
}