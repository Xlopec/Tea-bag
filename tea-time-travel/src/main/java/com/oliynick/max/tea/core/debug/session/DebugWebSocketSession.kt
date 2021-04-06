/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.protocol.*
import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*

@PublishedApi
internal class DebugWebSocketSession<M, S, J>(
    private val mClass: Class<M>,
    private val sClass: Class<S>,
    private val settings: ServerSettings<M, S, J>,
    private val socketSession: DefaultClientWebSocketSession
) : DebugSession<M, S, J> {

    private val incomingPackets: Flow<Either<M, S>> by unsafeLazy { settings.incomingCommands(socketSession) }

    override val messages: Flow<M> by unsafeLazy { incomingPackets.externalMessages() }
    override val states: Flow<S> by unsafeLazy { incomingPackets.externalStates() }

    override suspend fun invoke(packet: NotifyServer<J>) =
        socketSession.send(settings.serializer.toJson(packet))

    private fun ServerSettings<M, S, J>.incomingCommands(
        session: ClientWebSocketSession
    ) = incomingPackets(session)
        .map { packet -> serializer.toCommand(packet) }

    private fun JsonConverter<J>.toCommand(
        packet: NotifyClient<J>
    ) = when (val message = packet.message) {
        is ApplyMessage<J> -> Left(fromJsonTree(message.message, mClass))
        is ApplyState<J> -> Right(fromJsonTree(message.state, sClass))
    }

}

private sealed class Either<out L, out R>

private data class Left<L>(
    val l: L
) : Either<L, Nothing>()

private data class Right<R>(
    val r: R
) : Either<Nothing, R>()

private fun <S> Flow<Either<*, S>>.externalStates(): Flow<S> =
    filterIsInstance<Right<S>>().map { (s) -> s }

private fun <M> Flow<Either<M, *>>.externalMessages(): Flow<M> =
    filterIsInstance<Left<M>>().map { (m) -> m }

private fun <T> unsafeLazy(
    provider: () -> T
) = lazy(LazyThreadSafetyMode.NONE, provider)

private fun <M, S, J> ServerSettings<M, S, J>.incomingPackets(
    clientWebSocketSession: ClientWebSocketSession
): Flow<NotifyClient<J>> =
    clientWebSocketSession.incoming.broadcast().asFlow()
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> serializer.asNotifyClientPacket(json) }
        .filter { packet -> packet.component == id }

@Suppress("UNCHECKED_CAST")
private fun <J> JsonConverter<J>.asNotifyClientPacket(
    json: String
) = fromJson(json, NotifyClient::class.java) as NotifyClient<J>
