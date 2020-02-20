package protocol

@Deprecated("should be replaced with concrete type tailored for server/client usage case")
sealed class ClientMessage

data class ApplyMessage(
    val message: JsonTree
) : ClientMessage()

data class ApplyState(
    val state: JsonTree
) : ClientMessage()
