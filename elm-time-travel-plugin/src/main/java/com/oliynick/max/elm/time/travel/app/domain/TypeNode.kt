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

package com.oliynick.max.elm.time.travel.app.domain

import java.lang.reflect.Field
import java.lang.reflect.Type
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

sealed class TypeNode

data class FieldNode(val fieldName: String, val value: TypeNode)

data class AnyRef(val type: Type, val fields: Collection<FieldNode>) : TypeNode()

object NullRef : TypeNode()

sealed class Primitive<T> : TypeNode() {
    abstract val value: T?
}

data class IntPrimitive(override val value: Int?) : Primitive<Int>()

data class StringPrimitive(override val value: String?) : Primitive<String>()

data class IterablePrimitive(override val value: Iterable<*>, val fields: Collection<FieldNode>) : Primitive<Iterable<*>>()

data class MapPrimitive(override val value: Map<*, *>, val fields: Collection<FieldNode>) : Primitive<Map<*, *>>()

data class ArrayPrimitive(override val value: Array<*>, val fields: Collection<FieldNode>) : Primitive<Array<*>>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArrayPrimitive) return false

        if (!value.contentEquals(other.value)) return false
        if (fields != other.fields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + fields.hashCode()
        return result
    }
}

fun Any?.traverse(): TypeNode = when (this) {
    is String? -> StringPrimitive(this)
    is Int? -> IntPrimitive(this)
    is Byte? -> TODO()
    is Float? -> TODO()
    is Double? -> TODO()
    is Long? -> TODO()
    is Short? -> TODO()
    is Char? -> TODO()
    is Boolean? -> TODO()
    is Iterable<*> -> traverse()
    is Map<*, *> -> traverse()
    is Array<*> -> traverse()
    is Any -> AnyRef(this::class.java, collectNodes())
    null -> NullRef
    else -> throw IllegalArgumentException("What a terrible failure $this")
}

private fun Iterable<*>.traverse(): IterablePrimitive {
    return IterablePrimitive(this, mapIndexed { index, any -> any.traverse() named index })
}

private fun Array<*>.traverse(): ArrayPrimitive {
    return ArrayPrimitive(this, mapIndexed { index, any -> any.traverse() named index })
}

private fun Map<*, *>.traverse(): MapPrimitive {

    fun anyRef(index: Int, entry: Map.Entry<*, *>): FieldNode {
        return AnyRef(
            entry::class.java,
            listOf(
                entry.key.traverse() named "key",
                entry.value.traverse() named "value"
            )
        ) named index
    }

    return MapPrimitive(this, entries.mapIndexed(::anyRef))
}

private fun Any.collectNodes(): List<FieldNode> {
    return this::class.declaredMemberProperties
        .asSequence()
        .mapNotNull { property -> property.javaField }
        .mapNotNull { field -> field.value(this).traverse() named field.name }
        .toList()
}

private fun Field.value(of: Any): Any? {
    isAccessible = true
    return get(of)
}

private infix fun TypeNode.named(fieldName: String) = FieldNode(fieldName, this)

private infix fun TypeNode.named(fieldIndex: Int) = this named fieldIndex.toString()