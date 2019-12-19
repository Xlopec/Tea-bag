package protocol

sealed class ClientMessage

data class ApplyMessage(
    val messageValue: Any
) : ClientMessage()

data class ApplyState(
    val stateValue: Any
) : ClientMessage()
