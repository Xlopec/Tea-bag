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
import org.kodein.di.simpleErasedName
import javax.swing.tree.DefaultMutableTreeNode

fun TypeNode.toJTree(): DefaultMutableTreeNode {

    fun fillJTree(node: TypeNode, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(node)
            .also(parent::add)

        when (node) {
            is AnyRef -> node.fields.forEach { fillJTree(it.value, current) }
            is IterablePrimitive -> node.fields.forEach { fillJTree(it.value, current) }
            is MapPrimitive -> node.fields.forEach { fillJTree(it.value, current) }
            is ArrayPrimitive -> node.fields.forEach { fillJTree(it.value, current) }
            is NullRef, is IntPrimitive, is StringPrimitive -> Unit
        }.safe
    }

    return DefaultMutableTreeNode(this, true)
        .also { root -> fillJTree(this@toJTree, root) }
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

private fun toReadableString(field: FieldNode) = "${field.fieldName}=${field.value.toReadableString()}"

private val Primitive<*>.stringValueSafe
    get() = try {
        value?.toString() ?: "null"
    } catch (th: Throwable) {
        "Couldn't get value, method 'toString' thrown exception${th.message?.let { " with message: $it" }}"
    }