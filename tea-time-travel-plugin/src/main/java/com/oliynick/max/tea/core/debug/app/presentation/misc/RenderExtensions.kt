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

import com.oliynick.max.tea.core.debug.app.domain.cms.BooleanWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.ByteWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.CharWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.CollectionWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.DoubleWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.FloatWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.IntWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.LongWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.Null
import com.oliynick.max.tea.core.debug.app.domain.cms.Property
import com.oliynick.max.tea.core.debug.app.domain.cms.Ref
import com.oliynick.max.tea.core.debug.app.domain.cms.ShortWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.Snapshot
import com.oliynick.max.tea.core.debug.app.domain.cms.StringWrapper
import com.oliynick.max.tea.core.debug.app.domain.cms.Value
import com.oliynick.max.tea.core.debug.app.domain.cms.isPrimitive
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.getIcon
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
        is Null -> null
    }

private operator fun DefaultMutableTreeNode.plusAssign(children: Iterable<MutableTreeNode>) =
    children.forEach(::add)

private operator fun DefaultMutableTreeNode.plusAssign(child: MutableTreeNode) = add(child)

private fun Value.toReadableString(): String = when (this) {
    is StringWrapper -> toReadableString()
    is Null -> toReadableString()
    is Ref -> toReadableString()
    is CollectionWrapper -> toReadableString()
    is IntWrapper -> value.toString()
    is ByteWrapper -> value.toString()
    is ShortWrapper -> value.toString()
    is CharWrapper -> value.toString()
    is LongWrapper -> value.toString()
    is DoubleWrapper -> value.toString()
    is FloatWrapper -> value.toString()
    is BooleanWrapper -> value.toString()
}

@Suppress("unused")
private fun Null.toReadableString(): String = "null"

private fun StringWrapper.toReadableString(): String = "\"$value\""

private fun Ref.toReadableString(): String =
    "${type.name}(${properties.joinToString(transform = ::toReadableString, limit = 120)})"

private fun toReadableString(field: Property): String =
    "${field.name}=${field.v.toReadableString()}"

private fun CollectionWrapper.toReadableString(): String =
    value.joinToString(
        prefix = "[",
        postfix = "]",
        transform = { it.toReadableString() }
    )

private val Value.icon: Icon?
    inline get() = when {
        isPrimitive -> getIcon("variable")
        this is CollectionWrapper || this is Ref -> getIcon("class")
        else -> null
    }
