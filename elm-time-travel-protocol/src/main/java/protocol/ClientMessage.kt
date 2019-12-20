package protocol

sealed class ClientMessage

data class ApplyMessage(
    val message: Any
) : ClientMessage()

data class ApplyState(
    val state: Any
) : ClientMessage()
