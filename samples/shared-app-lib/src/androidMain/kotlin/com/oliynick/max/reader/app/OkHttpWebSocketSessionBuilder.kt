package com.oliynick.max.reader.app

import com.google.gson.JsonElement
import com.oliynick.max.entities.shared.datatypes.Left
import com.oliynick.max.entities.shared.datatypes.Right
import com.oliynick.max.reader.app.message.Message
import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.protocol.JsonConverter
import com.oliynick.max.tea.core.debug.protocol.NotifyClient
import com.oliynick.max.tea.core.debug.protocol.NotifyServer
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString

class OkHttpWebSocketSessionBuilder : SessionBuilder<Message, AppState, JsonElement> {

    private val client by lazy {
        OkHttpClient.Builder().build()
    }

    override suspend fun invoke(
        settings: ServerSettings<Message, AppState, JsonElement>,
        session: suspend DebugSession<Message, AppState, JsonElement>.() -> Unit
    ) {

        val adapter = WebSocketListenerAdapter()

        val ws = client.newWebSocket(
            Request.Builder()
                .url(settings.url.toString())
                .get()
                .build(),
            adapter
        )

        session.invoke(OkHttpWebSocketSession(adapter.packets, ws, settings))
        adapter.packets.collect()
        println("End of session")
    }
}

private class WebSocketListenerAdapter : WebSocketListener() {

    private val channel = Channel<String>()

    val packets: Flow<String> = channel.receiveAsFlow()

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("Closed $code, $reason")
        channel.close()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        println("Closing $code, $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
        println("Failure $response, ")
        channel.close(t)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("On message text - $text")
        channel.trySend(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("On message bytes $bytes")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("On open $response")
    }
}

internal class OkHttpWebSocketSession(
    frames: Flow<String>,
    private val webSocket: WebSocket,
    private val settings: ServerSettings<Message, AppState, JsonElement>,
) : DebugSession<Message, AppState, JsonElement> {

    private val packets = frames
        .map { json -> settings.serializer.asNotifyClientPacket(json) }
        .filter { packet -> packet.component == settings.id }

    override val messages: Flow<Message> = packets.filterIsInstance<Left<Message>>()
        .map { (m) -> m }
    override val states: Flow<AppState> = packets.filterIsInstance<Right<AppState>>()
        .map { (s) -> s }

    override suspend fun invoke(packet: NotifyServer<JsonElement>) {
        println("Sending packet $packet")
        webSocket.send(settings.serializer.toJson(packet))
    }

}

@Suppress("UNCHECKED_CAST")
private fun <J> JsonConverter<J>.asNotifyClientPacket(
    json: String
) = fromJson(json, NotifyClient::class) as NotifyClient<J>
