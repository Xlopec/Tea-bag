package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.converter.JsonConverter
import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*
import protocol.ApplyMessage
import protocol.ApplyState
import protocol.NotifyClient
import protocol.NotifyServer

private sealed class Either<out L, out R>

private data class Left<L>(
    val l: L
) : Either<L, Nothing>()

private data class Right<R>(
    val r: R
) : Either<Nothing, R>()

@PublishedApi
internal class DebugWebSocketSession<M, S>(
    private val mClass: Class<M>,
    private val sClass: Class<S>,
    private val settings: ServerSettings<M, S>,
    private val socketSession: DefaultClientWebSocketSession
) : DebugSession<M, S> {

    private val incomingPackets: Flow<Either<M, S>> by unsafeLazy { settings.incomingCommands(socketSession) }

    override val messages: Flow<M> by unsafeLazy { incomingPackets.externalMessages() }
    override val states: Flow<S> by unsafeLazy { incomingPackets.externalStates() }

    override suspend fun invoke(packet: NotifyServer) =
        socketSession.send(settings.serializer.toJson(packet))

    private fun ServerSettings<M, S>.incomingCommands(
        session: ClientWebSocketSession
    ) = incomingPackets(session)
        .map { packet -> serializer.toCommand(packet) }

    private fun JsonConverter.toCommand(
        packet: NotifyClient
    ) = when (val message = packet.message) {
        is ApplyMessage -> Left(
            fromJsonTree(
                message.message,
                mClass
            )
        )
        is ApplyState -> Right(
            fromJsonTree(
                message.state,
                sClass
            )
        )
    }

}

private fun <S> Flow<Either<*, S>>.externalStates(): Flow<S> =
    filterIsInstance<Right<S>>().map { (s) -> s }

private fun <M> Flow<Either<M, *>>.externalMessages(): Flow<M> =
    filterIsInstance<Left<M>>().map { (m) -> m }

private fun <T> unsafeLazy(
    provider: () -> T
) = lazy(LazyThreadSafetyMode.NONE, provider)

private fun <M, S> ServerSettings<M, S>.incomingPackets(
    clientWebSocketSession: ClientWebSocketSession
) =
    clientWebSocketSession.incoming.broadcast().asFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> serializer.fromJson(json, NotifyClient::class.java) }
        .filter { packet -> packet.component == id }
