package com.oliynick.max.elm.time.travel.app

import com.oliynick.max.elm.time.travel.app.exception.installErrorInterceptors
import com.oliynick.max.elm.time.travel.protocol.ApplyCommands
import com.oliynick.max.elm.time.travel.protocol.SendPacket
import com.oliynick.max.elm.time.travel.protocol.SomeTestCommand
import com.oliynick.max.elm.time.travel.protocol.SomeTestString
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DataConversion
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.*
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.websocket.webSocket
import org.slf4j.event.Level
import java.time.Duration



fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ConditionalHeaders)
    install(Locations)
    install(DataConversion)

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(5)
    }

    installErrorInterceptors()

    routing {

        webSocket("/") {



           /* val schema = RuntimeSchema.getSchema(Wrapper::class.java)

            val buffer = LinkedBuffer.allocate(512)

            // ser
            val protostuff: ByteArray
            try {
                val buffer1 = LinkedBuffer.allocate(512)

                val schema1 = RuntimeSchema.getSchema(SomeTestCommand::class.java)

               val cmd = SomeTestCommand(SomeTestString("something"))

                protostuff = ProtostuffIOUtil.toByteArray(Wrapper(SomeTestCommand::class.java, ProtostuffIOUtil.toByteArray(cmd, schema1, buffer1)), schema, buffer)
            } finally {
                buffer.clear()
            }*/

            val packet = SendPacket.pack("to some component", ApplyCommands(SomeTestCommand(SomeTestString("something"))))

            send(packet)

            //val json = gson.toJson(SendPacket(UUID.randomUUID(), "apply_message", ApplyCommands(SomeTestCommand(SomeTestString("something")))))

            //send(json)

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        outgoing.send(Frame.Text("YOU SAID: $text"))
                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                }
            }
        }
    }
}