package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.time.travel.protocol.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val httpClient by lazy { HttpClient { install(WebSockets) } }

data class Settings(val id: ComponentId, val host: String = "localhost", val port: UInt = 8080U)

fun <M : Any, C : Any, S : Any> CoroutineScope.component(settings: Settings,
                                                         initialState: S,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor,
                                                         vararg initialCommands: C): Component<M, S> {

    val snapshots = Channel<ComponentSnapshot>()
    val component = component(initialState, resolver, update, spyingInterceptor<M, C, S>(snapshots).with(interceptor), *initialCommands)

    launch {

        httpClient.ws(HttpMethod.Get, settings.host, settings.port.toInt()) {

            launch {
                for (snapshot in snapshots) {
                    send(SendPacket.pack(settings.id, snapshot))
                }
            }

            for (packet in incoming.of(Frame.Binary::class).map { ReceivePacket.unpack(it.readBytes()) }) {

                when (val action = packet.action) {
                    // apply messages
                    is ApplyCommands -> component(action).first()
                }
            }
        }
    }

    return component
}

private operator fun <M : Any, S : Any> Component<M, S>.invoke(action: ApplyCommands): Flow<S> {
    // apply messages
    @Suppress("UNCHECKED_CAST")
    return invoke(action.commands as List<M>)
}

private fun <M, S> Component<M, S>.invoke(args: List<M>): Flow<S> {
    return this(args.asFlow())
}

private fun <M : Any, C : Any, S : Any> spyingInterceptor(sink: SendChannel<ComponentSnapshot>): Interceptor<M, S, C> {
    return { message, prevState, newState, _ -> sink.send(ComponentSnapshot(message, prevState, newState)) }
}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
private suspend fun emptyInterceptor(message: Any, prevState: Any, newState: Any, commands: Set<*>) = Unit

fun <T> Flow<T>.mergeWith(other: Flow<T>): Flow<T> =
    channelFlow {
        coroutineScope {
            launch {
                other.collect {
                    offer(it)
                }
            }

            launch {
                collect {
                    offer(it)
                }
            }
        }
    }

