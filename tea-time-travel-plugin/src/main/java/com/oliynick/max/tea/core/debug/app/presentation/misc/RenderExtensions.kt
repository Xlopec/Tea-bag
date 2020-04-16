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

package com.oliynick.max.tea.core.debug.app.presentation.misc

import com.oliynick.max.tea.core.debug.app.domain.BooleanWrapper
import com.oliynick.max.tea.core.debug.app.domain.ByteWrapper
import com.oliynick.max.tea.core.debug.app.domain.CharWrapper
import com.oliynick.max.tea.core.debug.app.domain.CollectionWrapper
import com.oliynick.max.tea.core.debug.app.domain.DoubleWrapper
import com.oliynick.max.tea.core.debug.app.domain.FilteredSnapshot
import com.oliynick.max.tea.core.debug.app.domain.FloatWrapper
import com.oliynick.max.tea.core.debug.app.domain.IntWrapper
import com.oliynick.max.tea.core.debug.app.domain.LongWrapper
import com.oliynick.max.tea.core.debug.app.domain.Null
import com.oliynick.max.tea.core.debug.app.domain.Property
import com.oliynick.max.tea.core.debug.app.domain.Ref
import com.oliynick.max.tea.core.debug.app.domain.ShortWrapper
import com.oliynick.max.tea.core.debug.app.domain.StringWrapper
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.domain.isPrimitive
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueIcon.CLASS_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueIcon.PROPERTY_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueIcon.VARIABLE_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueIcon.WATCH_ICON
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

typealias ValueFormatter = Value.() -> String

private val DATE_TIME_FORMATTER: DateTimeFormatter by lazy {
    DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.MEDIUM
    )
}

fun Value.toJTree(): DefaultMutableTreeNode =
    when (this) {
        is Null -> toJTree()
        is CollectionWrapper -> toJTree()
        is Ref -> toJTree()
        is IntWrapper -> primitive(this)
        is ByteWrapper -> primitive(this)
        is ShortWrapper -> primitive(this)
        is CharWrapper -> primitive(this)
        is LongWrapper -> primitive(this)
        is DoubleWrapper -> primitive(this)
        is FloatWrapper -> primitive(this)
        is StringWrapper -> primitive(this)
        is BooleanWrapper -> primitive(this)
    }


fun FilteredSnapshot.toReadableString(formatter: DateTimeFormatter = DATE_TIME_FORMATTER): String =
    "${meta.timestamp.format(formatter)}: ${meta.id.value}"

fun PropertyNode.toReadableString(
    formatter: ValueFormatter
): String =
    "${property.name}=${property.v.run(formatter)}"

fun ValueNode.toReadableString(
    formatter: ValueFormatter
): String =
    value.run(formatter)

fun IndexedNode.toReadableString(
    formatter: ValueFormatter
): String =
    "[$index] = ${value.run(formatter)}"

fun EntryKeyNode.toReadableString(
    formatter: ValueFormatter
): String =
    "key = ${key.run(formatter)}"

fun EntryValueNode.toReadableString(
    formatter: ValueFormatter
): String =
    "value = ${value.run(formatter)}"

val RenderTree.icon: Icon?
    get() = when (this) {
        RootNode, is MessageNode, is StateNode -> null
        is SnapshotNode -> WATCH_ICON
        is PropertyNode -> PROPERTY_ICON
        is ValueNode -> value.icon
        is IndexedNode -> value.icon
        is EntryKeyNode -> key.icon
        is EntryValueNode -> value.icon
    }

fun RenderTree.toReadableString(
    model: TreeModel,
    formatter: ValueFormatter
): String {
    return when (this) {
        RootNode -> "Snapshots (${model.getChildCount(model.root)})"
        is SnapshotNode -> snapshot.toReadableString()
        is MessageNode -> "Message"
        is StateNode -> "State"
        is PropertyNode -> toReadableString(formatter)
        is ValueNode -> toReadableString(formatter)
        is IndexedNode -> toReadableString(formatter)
        is EntryKeyNode -> toReadableString(formatter)
        is EntryValueNode -> toReadableString(formatter)
    }
}

fun toReadableStringDetailed(
    value: Value
): String =
    when (value) {
        is StringWrapper -> value.toReadableString()
        is Null -> value.toReadableString()
        is Ref -> value.toReadableString()
        is CollectionWrapper -> value.toReadableString()
        is IntWrapper -> value.value.toString()
        is ByteWrapper -> value.value.toString()
        is ShortWrapper -> value.value.toString()
        is CharWrapper -> value.value.toString()
        is LongWrapper -> value.value.toString()
        is DoubleWrapper -> value.value.toString()
        is FloatWrapper -> value.value.toString()
        is BooleanWrapper -> value.value.toString()
    }

fun toReadableStringShort(
    value: Value
): String =
    when (value) {
        is StringWrapper -> value.toReadableString()
        is Null -> value.toReadableString()
        is Ref -> value.toReadableStringShort()
        is CollectionWrapper -> value.toReadableStringShort()
        is IntWrapper -> value.value.toString()
        is ByteWrapper -> value.value.toString()
        is ShortWrapper -> value.value.toString()
        is CharWrapper -> value.value.toString()
        is LongWrapper -> value.value.toString()
        is DoubleWrapper -> value.value.toString()
        is FloatWrapper -> value.value.toString()
        is BooleanWrapper -> value.value.toString()
    }

private fun primitive(
    value: Value
): DefaultMutableTreeNode =
    DefaultMutableTreeNode(ValueNode(value))

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

private fun Value.tryAppendSubTree(parent: DefaultMutableTreeNode): DefaultMutableTreeNode? =
    when (this) {
        is CollectionWrapper -> toJTree(parent)
        is Ref -> toJTree(parent)
        is IntWrapper,
        is ByteWrapper,
        is ShortWrapper,
        is CharWrapper,
        is LongWrapper,
        is DoubleWrapper,
        is FloatWrapper,
        is StringWrapper,
        is BooleanWrapper,
        is Null
        -> null
    }

private operator fun DefaultMutableTreeNode.plusAssign(children: Iterable<MutableTreeNode>) =
    children.forEach(::add)

private operator fun DefaultMutableTreeNode.plusAssign(child: MutableTreeNode) = add(child)

@Suppress("unused")
private fun Null.toReadableString(): String = "null"

private fun StringWrapper.toReadableString(): String = "\"$value\""

private fun Ref.toReadableString(): String =
    "${type.name}(${properties.joinToString(transform = ::toReadableString, limit = 120)})"

private fun Ref.toReadableStringShort(): String = type.name

private fun toReadableString(field: Property): String =
    "${field.name}=${toReadableStringDetailed(field.v)}"

private fun CollectionWrapper.toReadableString(): String =
    value.joinToString(
        prefix = "[",
        postfix = "]",
        transform = { toReadableStringDetailed(it) }
    )

private fun CollectionWrapper.toReadableStringShort(): String =
    "[${value.size} element${if(value.size == 1) "" else "s"}]"

private val Value.icon: Icon?
    inline get() = when {
        isPrimitive -> VARIABLE_ICON
        this is CollectionWrapper || this is Ref -> CLASS_ICON
        else -> null
    }

