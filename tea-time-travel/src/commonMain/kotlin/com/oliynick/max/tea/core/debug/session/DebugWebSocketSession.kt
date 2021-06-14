/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.protocol.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass

@PublishedApi
internal class DebugWebSocketSession<M : Any, S : Any, J>(
    private val mClass: KClass<M>,
    private val sClass: KClass<S>,
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
) = fromJson(json, NotifyClient::class) as NotifyClient<J>
