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

import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.sidebar.getIcon
import org.kodein.di.simpleErasedName
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode

private val DATE_TIME_FORMATTER: DateTimeFormatter by lazy { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }

fun <T> TypeNode.toJTree(mapper: (TypeNode) -> T): DefaultMutableTreeNode {

    fun fillJTree(node: TypeNode, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(mapper(node))
            .also(parent::add)

        node.children.forEach { child -> fillJTree(child, current) }
    }

    return DefaultMutableTreeNode(mapper(this), true)
        .also { root -> children.forEach { node -> fillJTree(node, root) } }
}

fun TypeNode.toJTree(): DefaultMutableTreeNode = toJTree(::identity)

val TypeNode.children: Collection<TypeNode>
    inline get() {
        val fields = when (this) {
            is AnyRef -> fields
            is IterablePrimitive -> fields
            is MapPrimitive -> fields
            is ArrayPrimitive -> fields
            is NullRef, is IntPrimitive, is StringPrimitive -> emptyList()
        }

        return fields.map { it.value }
    }

fun TypeNode.toReadableString(): String = when (this) {
    is AnyRef -> toReadableString()
    is IntPrimitive -> toReadableString()
    is StringPrimitive -> toReadableString()
    is IterablePrimitive -> toReadableString()
    is NullRef -> toReadableString()
    is MapPrimitive -> toReadableString()
    is ArrayPrimitive -> toReadableString()
}

fun AnyRef.toReadableString(): String {
    return "${type.simpleErasedName()}(${fields.joinToString(transform = ::toReadableString)})"
}

fun IterablePrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableString
    )}"
}

fun ArrayPrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableString
    )}"
}

fun MapPrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "{",
        postfix = "}",
        transform = ::toReadableString
    )}"
}

fun NullRef.toReadableString(): String = "null"

fun IntPrimitive.toReadableString() = "Int:$stringValueSafe"

fun StringPrimitive.toReadableString() = "String:${if (value == null) null else '"' + stringValueSafe + '"'}"

val TypeNode.icon: Icon?
    inline get() = when (this) {
        is ArrayPrimitive, is MapPrimitive, is IterablePrimitive, is AnyRef -> getIcon("class")
        is IntPrimitive, is StringPrimitive -> getIcon("variable")
        NullRef -> null
    }

fun Snapshot.toReadableString(formatter: DateTimeFormatter = DATE_TIME_FORMATTER): String {
    return "${timestamp.format(formatter)}: $id"
}

private fun toReadableString(field: FieldNode) = "${field.fieldName}=${field.value.toReadableString()}"

private val Primitive<*>.stringValueSafe
    get() = try {
        value?.toString() ?: "null"
    } catch (th: Throwable) {
        "Couldn't get value, method 'toString' thrown exception${th.message?.let { " with message: $it" }}"
    }

private fun identity(t: TypeNode) = t