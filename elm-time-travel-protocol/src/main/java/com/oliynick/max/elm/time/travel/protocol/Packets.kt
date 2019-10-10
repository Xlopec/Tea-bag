@file:Suppress("unused")

package com.oliynick.max.elm.time.travel.protocol

import io.protostuff.LinkedBuffer
import io.protostuff.ProtostuffIOUtil
import io.protostuff.Schema
import io.protostuff.runtime.RuntimeSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class SendPacket private constructor(val id: UUID,
                                     val component: String,
                                     val instanceClass: Class<out Action>,
                                     val instance: ByteArray) {
    companion object {

        private val buffer by lazy { LinkedBuffer.allocate(512) }
        private val buffer1 by lazy { LinkedBuffer.allocate(512) }

        fun pack(component: ComponentId, action: Action): ByteArray {

            val schema = schema<SendPacket>()
            val schema1 = schema(action::class.java)

            try {
                val packet = SendPacket(
                    UUID.randomUUID(),
                    component.id,
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

class ReceivePacket private constructor(val id: UUID, val component: ComponentId, val action: Action) {

    companion object {

        fun unpack(packet: ByteArray): ReceivePacket {
            return schema<SendPacket>()
                .parse(packet)
                .run { ReceivePacket(id, ComponentId(component), schema(instanceClass).parse(instance)) }
        }
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

private fun <T> Schema<@JvmSuppressWildcards T>.parse(data: ByteArray): T {
    return newMessage().also { t ->
        ProtostuffIOUtil.mergeFrom(data, t, this)
    }
}

private inline fun <reified T> schema(): Schema<@JvmSuppressWildcards T> {
    return RuntimeSchema.getSchema(T::class.java)
}

private fun <T> schema(clazz: Class<out T>): Schema<T> {
    return RuntimeSchema.getSchema(clazz) as Schema<T>
}