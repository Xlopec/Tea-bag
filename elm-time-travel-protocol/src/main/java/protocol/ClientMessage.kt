package protocol

sealed class ClientMessage

data class ApplyMessage(val messageValue: Value<*>) : ClientMessage()

data class ApplyState(val stateValue: Value<*>) : ClientMessage()