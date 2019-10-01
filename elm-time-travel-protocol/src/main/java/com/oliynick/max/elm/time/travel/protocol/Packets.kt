package com.oliynick.max.elm.time.travel.protocol

import io.protostuff.LinkedBuffer
import io.protostuff.ProtostuffIOUtil
import io.protostuff.Schema
import io.protostuff.runtime.RuntimeSchema
import java.util.*

sealed class Action

data class ApplyCommands(val commands: List<Any>) : Action() {

    constructor(command: Any) : this(listOf(command))

    init {
        require(commands.isNotEmpty())
    }
}

class SendPacket private constructor(
    val id: UUID,
    val component: String,
    val clazz: Class<out Action>,
    val actionData: ByteArray
) {
    companion object {

        private val buffer by lazy { LinkedBuffer.allocate(512) }
        private val buffer1 by lazy { LinkedBuffer.allocate(512) }

        suspend fun pack(component: String, action: Action): ByteArray {

            val schema = RuntimeSchema.getSchema(SendPacket::class.java)

            try {
                val schema1 = RuntimeSchema.getSchema(action::class.java) as Schema<Action>
                val packet = SendPacket(
                    UUID.randomUUID(),
                    component,
                    action::class.java,
                    ProtostuffIOUtil.toByteArray(action, schema1, buffer)
                )

                return ProtostuffIOUtil.toByteArray(packet, schema, buffer1)
            } finally {
                buffer.clear()
                buffer1.clear()
            }
        }


    }
}

class ReceivePacket private constructor(
    val id: UUID,
    val component: String,
    val action: Action
) {
    companion object {

        suspend fun unpack(packet: ByteArray): ReceivePacket {

            val wrapperSchema = RuntimeSchema.getSchema(SendPacket::class.java)

            val fooParsed = wrapperSchema.newMessage()

            ProtostuffIOUtil.mergeFrom(packet, fooParsed, wrapperSchema)

            val innerSchema = RuntimeSchema.getSchema(fooParsed.clazz) as Schema<Action>

            val lol = innerSchema.newMessage()

            ProtostuffIOUtil.mergeFrom(fooParsed.actionData, lol, innerSchema)

            println("Lol " + lol)

            return ReceivePacket(fooParsed.id, fooParsed.component, lol)
        }
    }
}


data class Wrapper(val clazz: Class<*>, val data: ByteArray)

data class SomeTestString(val value: String)

data class SomeTestState(val string: SomeTestString)

data class SomeTestCommand(val str: SomeTestString)