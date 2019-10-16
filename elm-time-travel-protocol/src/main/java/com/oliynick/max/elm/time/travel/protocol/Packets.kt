/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class SendPacket private constructor(val id: UUID,
                                     val component: String,
                                     val instanceClass: Class<out Message>,
                                     val instance: ByteArray) {
    companion object {

        private val buffer by lazy { LinkedBuffer.allocate(512) }
        private val mutex = Mutex()

        suspend fun pack(id: UUID, component: ComponentId, message: Message): ByteArray {
            mutex.withLock {
                try {
                    return ProtostuffIOUtil.toByteArray(sendPacket(id, component, message), schema<SendPacket>(), buffer.clear())
                } finally {
                    buffer.clear()
                }
            }
        }

        private fun sendPacket(id: UUID, component: ComponentId, message: Message): SendPacket {
            return SendPacket(
                id,
                component.id,
                message::class.java,
                ProtostuffIOUtil.toByteArray(message, schema(message::class.java), buffer)
            )
        }
    }
}

class ReceivePacket private constructor(val id: UUID, val component: ComponentId, val message: Message) {

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