package com.oliynick.max.elm.time.travel.app

import com.oliynick.max.elm.time.travel.app.exception.installErrorInterceptors
import com.oliynick.max.elm.time.travel.protocol.ApplyCommands
import com.oliynick.max.elm.time.travel.protocol.ReceivePacket
import com.oliynick.max.elm.time.travel.protocol.SendPacket
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
import io.ktor.websocket.webSocket
import org.slf4j.event.Level
import java.io.File
import java.lang.reflect.Field
import java.time.Duration
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField



fun main(args: Array<String>) /*= EngineMain.main(args)*/ {

}

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

    val loader = FileSystemClassLoader(File("/Users/user/AndroidStudioProjects/WeatherViewer/elm-time-travel/build/classes/"))

    routing {

        webSocket("/") {

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        outgoing.send(Frame.Text("YOU SAID: $text"))
                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                    is Frame.Binary -> {
                        require(frame.fin) { "Chunks aren't supported" }

                        Thread.currentThread().contextClassLoader = loader

                        val packet = ReceivePacket.unpack(frame.readBytes())

                        println(packet)

                        val cmd = (packet.action as ApplyCommands).commands.first()

                        cmd.updateStringFieldTo("replaced with")

                        println(cmd)

                        val response = SendPacket.pack("to some component", ApplyCommands(cmd))

                        send(response)
                    }
                }
            }
        }
    }
}

 fun Any.updateStringFieldTo(value: String) {

    fun updateField(inside: Any, field: Field) {
        if (field.type.isAssignableFrom(String::class.java)) {
            field.set(inside, value)
            return
        }

        val who = field.get(inside)

        field.type.kotlin.memberProperties.filter { it.javaField != null }.forEach {
            it.isAccessible = true
            it.javaField!!.isAccessible = true

            updateField(who, it.javaField!!)
        }
    }
    this::class.memberProperties.filter { it.javaField != null }.forEach {
        it.isAccessible = true
        it.javaField!!.isAccessible = true

        updateField(this, it.javaField!!)
    }
}