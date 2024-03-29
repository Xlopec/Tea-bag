/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.tea.time.travel.session

import arrow.core.Either
import io.github.xlopec.tea.time.travel.component.Settings
import io.github.xlopec.tea.time.travel.protocol.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass

@PublishedApi
internal class DebugWebSocketSession<M : Any, S : Any, J>(
    private val mClass: KClass<M>,
    private val sClass: KClass<S>,
    private val settings: Settings<M, S, J>,
    socketSession: WebSocketSession
) : DebugSession<M, S, J>, WebSocketSession by socketSession {

    private val incomingPackets: Flow<Either<M, S>> = incomingCommands(settings)

    override val messages: Flow<M> = incomingPackets.messages()
    override val states: Flow<S> = incomingPackets.states()

    override suspend fun invoke(packet: NotifyServer<J>) =
        send(settings.serializer.toJson(packet))

    private fun WebSocketSession.incomingCommands(
        settings: Settings<M, S, J>
    ) = incomingPackets(settings)
        .map { packet -> settings.serializer.toCommand(packet) }

    private fun JsonSerializer<J>.toCommand(
        packet: NotifyClient<J>
    ) = when (val message = packet.message) {
        is ApplyMessage<J> -> Either.Left(fromJsonTree(message.message, mClass))
        is ApplyState<J> -> Either.Right(fromJsonTree(message.state, sClass))
    }
}

private fun <S> Flow<Either<*, S>>.states(): Flow<S> =
    filterIsInstance<Either.Right<S>>().map { (s) -> s }

private fun <M> Flow<Either<M, *>>.messages(): Flow<M> =
    filterIsInstance<Either.Left<M>>().map { (m) -> m }

private fun <M, S, J> WebSocketSession.incomingPackets(
    settings: Settings<M, S, J>
): Flow<NotifyClient<J>> =
    incoming.receiveAsFlow().shareIn(this, SharingStarted.Lazily)
        .filterIsInstance<Frame.Text>()
        .map { frame -> frame.readText() }
        .map { json -> settings.serializer.asNotifyClientPacket(json) }
        .filter { packet -> packet.component == settings.id }

@Suppress("UNCHECKED_CAST")
private fun <J> JsonSerializer<J>.asNotifyClientPacket(
    json: String
) = fromJson(json, NotifyClient::class) as NotifyClient<J>
