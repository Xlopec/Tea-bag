package protocol

sealed class ClientMessage

data class ApplyMessage(
    val messageValue: Json
) : ClientMessage()

data class ApplyState(
    val stateValue: Json
) : ClientMessage()