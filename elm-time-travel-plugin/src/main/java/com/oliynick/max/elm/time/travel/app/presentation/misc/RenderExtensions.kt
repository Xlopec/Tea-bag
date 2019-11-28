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

package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.Snapshot
import com.oliynick.max.elm.time.travel.app.presentation.sidebar.getIcon
import org.kodein.di.simpleErasedName
import protocol.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode

private val DATE_TIME_FORMATTER: DateTimeFormatter by lazy {
    DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.MEDIUM
    )
}

fun <T> Value<*>.toJTree(mapper: (Value<*>) -> T): DefaultMutableTreeNode {

    fun fillJTree(node: Value<*>, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(mapper(node))
            .also(parent::add)

        node.children.forEach { child -> fillJTree(child, current) }
    }

    return DefaultMutableTreeNode(mapper(this), true)
        .also { root -> children.forEach { node -> fillJTree(node, root) } }
}

fun Value<*>.toJTree(): DefaultMutableTreeNode = toJTree(::identity)

fun anyRef(entry: Map.Entry<*, *>): Value<*> {
    return Ref(
        RemoteType(entry::class.java),
        setOf(
            Property(
                RemoteType(if (entry.key == null) Any::class.java else entry.key!!::class.java),
                "key",
                entry.key.toValue(converters())
            ),
            Property(
                RemoteType(if (entry.value == null) Any::class.java else entry.value!!::class.java),
                "value",
                entry.value.toValue(converters())
            )
        )
    )
}

val Value<*>.children: Collection<Value<*>>
    inline get() = when (this) {
        is PrimitiveWrapper<*>, is Null -> emptyList()
        is MapWrapper -> value.map(::anyRef)
        is Ref -> properties.map { it.v }
        is CollectionWrapper -> value.map { it }
    }

fun Value<*>.toReadableString(): String = when (this) {
    /*is AnyRef -> toReadableString()
    is IntPrimitive -> toReadableString()
    is StringPrimitive -> toReadableString()
    is IterablePrimitive -> toReadableString()
    is NullRef -> toReadableString()
    is MapPrimitive -> toReadableString()
    is ArrayPrimitive -> toReadableString()*/
    is IntWrapper -> toReadableString()
    is ByteWrapper -> toReadableString()
    is ShortWrapper -> toReadableString()
    is CharWrapper -> toReadableString()
    is LongWrapper -> toReadableString()
    is DoubleWrapper -> toReadableString()
    is FloatWrapper -> toReadableString()
    is StringWrapper -> toReadableString()
    is BooleanWrapper -> toReadableString()
    is MapWrapper -> toReadableString()
    is Null -> toReadableString()
    is Ref -> toReadableString()
    is CollectionWrapper -> toReadableString()
}

fun Ref.toReadableString(): String {
    return "${type.value}(${properties.joinToString(transform = ::toReadableString)})"
}

fun CollectionWrapper.toReadableString(): String {
    return "${type.value} ${value.joinToString(
        prefix = "[",
        postfix = "]",
        transform = { it.toReadableString() }
    )}"
}

/*fun ArrayPrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableString
    )}"
}*/

fun MapWrapper.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${value.entries.joinToString(
        prefix = "{",
        postfix = "}",
        transform = { e -> e.key.toReadableString() + " -> " + e.value.toReadableString() }
    )}"
}

fun Null.toReadableString(): String = "null"

fun IntWrapper.toReadableString() = "Int:$stringValueSafe"

fun LongWrapper.toReadableString() = "Long:$stringValueSafe"

fun FloatWrapper.toReadableString() = "Float:$stringValueSafe"

fun BooleanWrapper.toReadableString() = "Boolean:$stringValueSafe"

fun DoubleWrapper.toReadableString() = "Double:$stringValueSafe"

fun CharWrapper.toReadableString() = "Char:$stringValueSafe"

fun ShortWrapper.toReadableString() = "Short:$stringValueSafe"

fun ByteWrapper.toReadableString() = "Byte:$stringValueSafe"

fun StringWrapper.toReadableString() = "String:${'"' + stringValueSafe + '"'}"

val Value<*>.icon: Icon?
    inline get() = when (this) {
        is PrimitiveWrapper<*> -> getIcon("variable")
        is CollectionWrapper, is MapWrapper, is Ref -> getIcon("class")
        is Null -> null
    }

fun Snapshot.toReadableString(formatter: DateTimeFormatter = DATE_TIME_FORMATTER): String {
    return "${timestamp.format(formatter)}: $id"
}

private fun toReadableString(field: Property<*>) = "${field.name}=${field.v.toReadableString()}"

private val PrimitiveWrapper<*>.stringValueSafe
    get() = try {
        value?.toString() ?: "null"
    } catch (th: Throwable) {
        "Couldn't get value, method 'toString' thrown exception${th.message?.let { " with message: $it" }}"
    }

private fun identity(t: Value<*>) = t