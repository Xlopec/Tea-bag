package protocol

sealed class ClientMessage<out J>

data class ApplyMessage<out J>(
    val message: J
) : ClientMessage<J>()

data class ApplyState<J>(
    val state: J
) : ClientMessage<J>()
