package com.oliynick.max.elm.time.travel.app

import com.oliynick.max.elm.time.travel.app.exception.installErrorInterceptors
import com.oliynick.max.elm.time.travel.app.misc.safe
import com.oliynick.max.elm.time.travel.app.plugin.*
import com.oliynick.max.elm.time.travel.protocol.ApplyCommands
import com.oliynick.max.elm.time.travel.protocol.ReceivePacket
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DataConversion
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.slf4j.event.Level
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

fun server(settings: Settings, events: Channel<PluginMessage>): NettyApplicationEngine {

    return embeddedServer(Netty, port = settings.serverSettings.port.toInt()) {

        install(CallLogging) {
            level = Level.INFO
            filter { call -> call.request.path().startsWith("/") }
        }

        install(ConditionalHeaders)
        install(DataConversion)

        install(DefaultHeaders) { header("X-Engine", "Ktor") }

        install(io.ktor.websocket.WebSockets) {
            pingPeriod = Duration.ofSeconds(10)
            timeout = Duration.ofSeconds(5)
        }

        installErrorInterceptors()

        environment.monitor.subscribe(ApplicationStarting) { events.offer(NotifyStarting) }
        environment.monitor.subscribe(ApplicationStarted) { events.offer(NotifyStarted) }
        environment.monitor.subscribe(ApplicationStopping) { events.offer(NotifyStopping) }
        environment.monitor.subscribe(ApplicationStopped) { events.offer(NotifyStopped) }

        val loader = FileSystemClassLoader(settings.classFiles)

        routing {

            webSocket("/") {

                for (frame in incoming.of(Frame.Binary::class)) {

                    require(frame.fin) { "Chunks aren't supported" }
                    //fixme how to reload classes later?
                    Thread.currentThread().contextClassLoader = loader

                    events.send(ReceivePacket.unpack(frame.readBytes()).toMessage())
                }
            }
        }
    }
}

private fun ReceivePacket.toMessage(): PluginMessage {
    return when(val action = action) {
        is ApplyCommands -> AppendCommands(action.commands)
    }
}

fun <E : Any, R : E> ReceiveChannel<E>.of(of: KClass<R>, context: CoroutineContext = Dispatchers.Unconfined): ReceiveChannel<R> {
    return GlobalScope.produce(context) {
        for (e in this@of) {
            if (of.isInstance(e)) {
                @Suppress("UNCHECKED_CAST")
                send(e as R)
            }
        }
    }
}