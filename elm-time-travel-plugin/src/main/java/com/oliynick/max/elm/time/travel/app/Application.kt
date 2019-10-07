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