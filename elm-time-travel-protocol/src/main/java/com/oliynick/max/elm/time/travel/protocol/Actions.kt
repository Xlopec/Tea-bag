package com.oliynick.max.elm.time.travel.protocol

sealed class Message

sealed class ClientMessage : Message()

sealed class ServerMessage : Message()

data class ApplyMessage(val message: Any) : ClientMessage()

data class ApplyState(val state: Any) : ClientMessage()

data class NotifyComponentSnapshot(val message: Any, val oldState: Any, val newState: Any) : ServerMessage()

data class NotifyComponentAttached(val state: Any) : ServerMessage()