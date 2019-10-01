package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.protocol.ApplyCommands
import com.oliynick.max.elm.time.travel.protocol.Packet
import com.oliynick.max.elm.time.travel.protocol.gson
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class Settings(val name: String,
                    val host: String = "localhost",
                    val port: Int = 8080)


class ProxyComponent<M, S>(private val scope: CoroutineScope,
                           private val delegate: Component<M, S>) : Component<M, S> {

    init {

    }

    override fun invoke(messages: Flow<M>): Flow<S> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

fun <M : Any, S : Any> CoroutineScope.component(settings: Settings, component: Component<M, S>): Component<M, S> {

    launch {
        val client = HttpClient {
            install(WebSockets)
        }

        client.ws(
            method = HttpMethod.Get,
            host = settings.host,
            port = settings.port
        ) {

           // launch { component.changes().collect { s -> send(gson.toJson(Packet(UUID.randomUUID().toString(), "update", s))) } }

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        println(text)

                        val packet = gson.fromJson(text, Packet::class.java)

                        when(val action = packet.action) {
                            is ApplyCommands -> {
                                val message = action.commands as List<M>

                                val s = component.invoke(message).first()

                                println(s)
                            }
                        }

                    }
                    is Frame.Binary -> println(frame.readBytes())
                }
            }
        }
    }

    return component
}

private fun <M, S> Component<M, S>.invoke(args: List<M>): Flow<S> {
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

fun main() {
    runBlocking {

        component(
            Settings("webSocketComponent"),
            component<String, String, String>(
                "",
                { emptySet() },
                { message, _ -> message.noCommand() },
                androidLogger("Test"))
        )

    }
}