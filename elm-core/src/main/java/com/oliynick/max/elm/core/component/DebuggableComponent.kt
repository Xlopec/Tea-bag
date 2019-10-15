package com.oliynick.max.elm.core.component

import com.oliynick.max.elm.time.travel.protocol.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val httpClient by lazy { HttpClient { install(WebSockets) } }

data class Settings(val id: ComponentId, val host: String = "localhost", val port: UInt = 8080U)

fun <M : Any, C : Any, S : Any> CoroutineScope.component(
    settings: Settings,
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    interceptor: Interceptor<M, S, C> = ::emptyInterceptor,
    vararg initialCommands: C
): Component<M, S> {

    @Suppress("RedundantSuspendModifier")
    suspend fun loader() = initialState to setOf(*initialCommands)

    return component(settings, ::loader, resolver, update, interceptor)
}

fun <M : Any, C : Any, S : Any> CoroutineScope.component(
    settings: Settings,
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    interceptor: Interceptor<M, S, C> = ::emptyInterceptor
): Component<M, S> {

    val snapshots = Channel<NotifyComponentSnapshot>()
    val args = Dependencies(initializer, resolver, update, spyingInterceptor<M, C, S>(snapshots).with(interceptor))
    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)
    val messages = Channel<M>()

    suspend fun compute(s: S) = compute(s, messages.iterator(), args, statesChannel)

    launch {

        var computationJob = launch { compute(computeNonTransientState(args, statesChannel)) }

        httpClient.ws(HttpMethod.Get, settings.host, settings.port.toInt()) {

            launch {
                snapshots.consumeAsFlow()
                    .map { snapshot -> SendPacket.pack(settings.id, snapshot) }
                    .collect { packet -> send(packet) }
            }

            incoming.consumeAsFlow()
                .filterIsInstance<Frame.Binary>()
                .map { ReceivePacket.unpack(it.readBytes()) }
                .collect { packet ->

                    println("Packet $packet")

                    @Suppress("UNCHECKED_CAST")
                    when (val message = packet.message) {
                        // apply messages
                        is ApplyMessage -> messages.send(message.message as M)
                        is ApplyState -> {
                            // hard swap
                            computationJob.cancel()

                            val applyState = message.state as S

                            statesChannel.offerChecking(applyState)
                            computationJob = launch { compute(applyState) }
                            send(SendPacket.pack(settings.id, NotifyStateUpdated(applyState)))
                        }
                        // fixme separate em somehow
                        is NotifyStateUpdated -> notifyUnexpectedMessage(message)
                        is NotifyComponentSnapshot -> notifyUnexpectedMessage(message)
                    }.safe
                }
        }
    }

    return newComponent(statesChannel.asFlow(), messages)
}

private fun <M : Any, C : Any, S : Any> spyingInterceptor(sink: SendChannel<NotifyComponentSnapshot>): Interceptor<M, S, C> {
    return { message, prevState, newState, _ -> sink.send(NotifyComponentSnapshot(message, prevState, newState)) }
}