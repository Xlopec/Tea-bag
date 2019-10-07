package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.protocol.ApplyCommands
import com.oliynick.max.elm.time.travel.protocol.ReceivePacket
import com.oliynick.max.elm.time.travel.protocol.SendPacket
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL


data class Settings(val name: String,
                    val host: String = "localhost",
                    val port: Int = 8080)

inline fun <reified M : Any, S : Any> CoroutineScope.component(settings: Settings, noinline component: Component<M, S>): Component<M, S> {

    launch {
        val client = HttpClient {
            install(WebSockets)
        }

        client.ws(
            method = HttpMethod.Get,
            host = settings.host,
            port = settings.port
        ) {

           // launch { component.changes().collect { s -> send(gson.toJson(SendPacket(UUID.randomUUID().toString(), "update", s))) } }

            val packet = SendPacket.pack("to some component", ApplyCommands(SomeTestCommand(SomeTestString("something"),
                listOf(URL("https://www.youtube.com/watch?v=fwfjsDsMuz8"), "123" to 1))))

            send(packet)

            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val bytes = frame.readBytes()

                        println(bytes)

                        val packet = ReceivePacket.unpack(bytes)

                        println(packet)

                        when(val action = packet.action) {
                            is ApplyCommands -> {

                                val message = action.commands as List<M>

                                val s = component.invoke(message).first()

                                println(s)
                            }
                        }
                    }
                }
            }
        }
    }

    return component
}

fun <M, S> Component<M, S>.invoke(args: List<M>): Flow<S> {
    return this(args.asFlow())
}

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

