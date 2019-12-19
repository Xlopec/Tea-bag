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

import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.app.presentation.sidebar.getIcon
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

private val DATE_TIME_FORMATTER: DateTimeFormatter by lazy {
    DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.MEDIUM
    )
}

fun Value<*>.toJTree(): DefaultMutableTreeNode =
    when (this) {
        is PrimitiveWrapper<*> -> toJTree()
        is Null -> toJTree()
        is CollectionWrapper -> toJTree()
        is Ref -> toJTree()
    }

fun Snapshot.toReadableString(formatter: DateTimeFormatter = DATE_TIME_FORMATTER): String =
    "${timestamp.format(formatter)}: $id"

fun PropertyNode.toReadableString(): String =
    "${property.name}=${property.v.toReadableString()}"

fun ValueNode.toReadableString(): String =
    value.toReadableString()

fun IndexedNode.toReadableString(): String =
    "[$index] = ${value.toReadableString()}"

fun EntryKeyNode.toReadableString(): String =
    "key = ${key.toReadableString()}"

fun EntryValueNode.toReadableString(): String =
    "value = ${value.toReadableString()}"

val RenderTree.icon: Icon?
    get() = when (this) {
        RootNode, is MessageNode, is StateNode -> null
        is SnapshotNode -> getIcon("watch")
        is PropertyNode -> getIcon("property")
        is ValueNode -> value.icon
        is IndexedNode -> value.icon
        is EntryKeyNode -> key.icon
        is EntryValueNode -> value.icon
    }

private fun PrimitiveWrapper<*>.toJTree(): DefaultMutableTreeNode =
    DefaultMutableTreeNode(ValueNode(this))

private fun Null.toJTree(): DefaultMutableTreeNode =
    DefaultMutableTreeNode(ValueNode(this))

private fun Ref.toJTree(
    parent: DefaultMutableTreeNode = DefaultMutableTreeNode(ValueNode(this))
): DefaultMutableTreeNode =
    properties.fold(parent) { acc, property ->

        val propertyNode = DefaultMutableTreeNode(PropertyNode(property))

        property.v.tryAppendSubTree(propertyNode)

        acc += propertyNode
        acc
    }

private fun CollectionWrapper.toJTree(
    parent: DefaultMutableTreeNode = DefaultMutableTreeNode(ValueNode(this))
): DefaultMutableTreeNode =
    value.foldIndexed(parent) { index, acc, value ->

        val indexedNode = DefaultMutableTreeNode(IndexedNode(index, value))

        value.tryAppendSubTree(indexedNode)

        acc += indexedNode
        acc
    }

private fun Value<*>.tryAppendSubTree(parent: DefaultMutableTreeNode): DefaultMutableTreeNode? =
    when (this) {
        is PrimitiveWrapper<*>, is Null -> null
        is CollectionWrapper -> toJTree(parent)
        is Ref -> toJTree(parent)
    }

private operator fun DefaultMutableTreeNode.plusAssign(children: Iterable<MutableTreeNode>) =
    children.forEach(::add)

private operator fun DefaultMutableTreeNode.plusAssign(child: MutableTreeNode) = add(child)

private fun Value<*>.toReadableString(): String = when (this) {
    is StringWrapper -> toReadableString()
    is PrimitiveWrapper<*> -> value.toString()
    is Null -> toReadableString()
    is Ref -> toReadableString()
    is CollectionWrapper -> toReadableString()
}

private fun Null.toReadableString(): String = "null"

private fun StringWrapper.toReadableString(): String = '"' + value + '"'

private fun Ref.toReadableString(): String =
    "Object:(${properties.joinToString(transform = ::toReadableString, limit = 200)})"

private fun toReadableString(field: Property<*>): String =
    "Property: ${field.name}=${field.v.toReadableString()}"

private fun CollectionWrapper.toReadableString(): String {
    return value.joinToString(
        prefix = "[",
        postfix = "]",
        transform = { it.toReadableString() }
    )
}

private val Value<*>.icon: Icon?
    inline get() = when (this) {
        is PrimitiveWrapper<*> -> getIcon("variable")
        is CollectionWrapper, is Ref -> getIcon("class")
        is Null -> null
    }
