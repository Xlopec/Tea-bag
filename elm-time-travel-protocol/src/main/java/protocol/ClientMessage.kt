package protocol

@Deprecated("remove")
sealed class ClientMessage

data class ApplyMessage(
    val message: JsonTree
) : ClientMessage()

data class ApplyState(
    val state: JsonTree
) : ClientMessage()
