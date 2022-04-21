/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc

import io.github.xlopec.tea.time.travel.plugin.domain.BooleanWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.CharWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.domain.Null
import io.github.xlopec.tea.time.travel.plugin.domain.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.Property
import io.github.xlopec.tea.time.travel.plugin.domain.Ref
import io.github.xlopec.tea.time.travel.plugin.domain.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.Value
import io.github.xlopec.tea.time.travel.plugin.domain.isPrimitive
import io.github.xlopec.tea.time.travel.plugin.domain.primitiveTypeName
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.ClassIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.PropertyIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.VariableIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.WatchIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree.CollectionNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree.Leaf
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree.Node
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree.RefNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree.SnapshotINode
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel
import org.codehaus.groovy.runtime.wrappers.FloatWrapper

typealias ValueFormatter = Value.() -> String

val DATE_TIME_FORMATTER: DateTimeFormatter by lazy {
    DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.MEDIUM
    )
}

/*fun Value.toRenderTree(): RenderTree =
    when (this) {
        is Null -> ValueNode(this)
        is CollectionWrapper -> toJTree()
        is Ref -> toJTree()
        is NumberWrapper -> ValueNode(this)
        is CharWrapper -> ValueNode(this)
        is FloatWrapper -> ValueNode(this)
        is StringWrapper -> ValueNode(this)
        is BooleanWrapper -> ValueNode(this)
    }*/

fun Value.toJTree(): DefaultMutableTreeNode =
    when (this) {
        is Null -> toJTree()
        is CollectionWrapper -> toJTree()
        is Ref -> toJTree()
        is NumberWrapper -> Primitive(this)
        is CharWrapper -> Primitive(this)
        is FloatWrapper -> Primitive(this)
        is StringWrapper -> Primitive(this)
        is BooleanWrapper -> Primitive(this)
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
        is SnapshotNode -> WatchIcon
        is PropertyNode -> PropertyIcon
        is ValueNode -> value.icon
        is IndexedNode -> value.icon
        is EntryKeyNode -> key.icon
        is EntryValueNode -> value.icon
    }

fun RenderTree.toReadableString(
    model: TreeModel,
    formatter: ValueFormatter
): String =
    when (this) {
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

fun toReadableStringDetailed(
    value: Value
): String =
    when (value) {
        is StringWrapper -> value.toReadableString()
        is Null -> value.toReadableString()
        is Ref -> value.toReadableString()
        is CollectionWrapper -> value.toReadableString()
        is NumberWrapper -> value.toReadableString()
        is CharWrapper -> value.toReadableString()
        is BooleanWrapper -> value.toReadableString()
    }

fun toReadableStringShort(
    value: Value
): String =
    when (value) {
        is StringWrapper -> value.toReadableString()
        is Null -> value.toReadableString()
        is Ref -> value.toReadableStringShort()
        is CollectionWrapper -> value.toReadableStringShort()
        is NumberWrapper -> value.toReadableString()
        is CharWrapper -> value.toReadableString()
        is BooleanWrapper -> value.toReadableString()
    }

private fun Primitive(
    value: Value
): DefaultMutableTreeNode =
    DefaultMutableTreeNode(ValueNode(value))

private fun Null.toJTree(): DefaultMutableTreeNode =
    DefaultMutableTreeNode(ValueNode(this))

/*
private fun Ref.toRenderTree(
    parent: RenderTree = ValueNode(this)
): RenderTree =
    properties.fold(parent) { acc, property ->

        val propertyNode = PropertyNode(property)

        property.v.tryAppendSubTree(propertyNode)

        acc += propertyNode
        acc
    }
*/

private fun Ref.toJTree(
    parent: DefaultMutableTreeNode = DefaultMutableTreeNode(ValueNode(this))
): DefaultMutableTreeNode =
    properties.fold(parent) { acc, property ->

        val propertyNode = DefaultMutableTreeNode(PropertyNode(property))

        property.v.tryAppendSubTree(propertyNode)

        acc += propertyNode
        acc
    }

/*private fun CollectionWrapper.toRenderTree(
    parent: RenderTree = ValueNode(this)
) = value.mapIndexed { index, value ->
        val indexedNode = IndexedNode(index, value)

        value.tryAppendSubTree(indexedNode)
    }*/

private fun CollectionWrapper.toJTree(
    parent: DefaultMutableTreeNode = DefaultMutableTreeNode(ValueNode(this))
): DefaultMutableTreeNode =
    items.foldIndexed(parent) { index, acc, value ->

        val indexedNode = DefaultMutableTreeNode(IndexedNode(index, value))

        value.tryAppendSubTree(indexedNode)

        acc += indexedNode
        acc
    }

/*private fun Value.tryAppendSubTree(parent: PropertyNode): RenderTree? =
    when (this) {
        is CollectionWrapper -> toRenderTree(parent)
        is Ref -> toRenderTree(parent)
        is CharWrapper,
        is NumberWrapper,
        is StringWrapper,
        is BooleanWrapper,
        is Null
        -> null
    }*/

private fun Value.tryAppendSubTree(parent: DefaultMutableTreeNode): DefaultMutableTreeNode? =
    when (this) {
        is CollectionWrapper -> toJTree(parent)
        is Ref -> toJTree(parent)
        is CharWrapper,
        is NumberWrapper,
        is StringWrapper,
        is BooleanWrapper,
        is Null
        -> null
    }
/*
private fun Value.tryAppendSubTree(parent: RenderTree): RenderTree? =
    when (this) {
        is CollectionWrapper -> toRenderTree(parent)
        is Ref -> toRenderTree(parent)
        is CharWrapper,
        is NumberWrapper,
        is StringWrapper,
        is BooleanWrapper,
        is Null
        -> null
    }*/

private operator fun DefaultMutableTreeNode.plusAssign(children: Iterable<MutableTreeNode>) =
    children.forEach(::add)

private operator fun DefaultMutableTreeNode.plusAssign(child: MutableTreeNode) = add(child)

@Suppress("unused")
internal fun Null.toReadableString(): String = "null"

internal fun StringWrapper.toReadableString(): String = "\"$value\":$primitiveTypeName"

internal fun Ref.toReadableString(): String =
    "${type.name}(${properties.joinToString(transform = ::toReadableString, limit = 120)})"

internal fun Ref.toReadableStringShort(): String = type.name

internal fun toReadableString(field: Property): String =
    "${field.name}=${toReadableStringDetailed(field.v)}"

internal fun toReadableStringShort(field: Property): String =
    "${field.name}=${toReadableStringShort(field.v)}"

internal fun CollectionWrapper.toReadableString(): String =
    items.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableStringDetailed
    )

internal fun CollectionWrapper.toReadableStringShort(): String =
    "[${items.size} element${if (items.size == 1) "" else "s"}]"

internal fun NumberWrapper.toReadableString(): String =
    "$value:$primitiveTypeName"

internal fun CharWrapper.toReadableString(): String = "$value:$primitiveTypeName"

internal fun BooleanWrapper.toReadableString(): String = "$value:$primitiveTypeName"

private val Value.icon: Icon?
    inline get() = when {
        isPrimitive -> VariableIcon
        this is CollectionWrapper || this is Ref -> ClassIcon
        else -> null
    }

fun toReadableStringDetailed(
    node: Node
): String =
    when (node) {
        is CollectionNode -> node.children.joinToString(
            prefix = "[",
            postfix = "]",
            transform = ::toReadableStringDetailed
        )
        is Leaf -> toReadableStringDetailed(node.value)
        is RefNode -> node.type.name
        is SnapshotINode -> "${node.meta.timestamp.format(DATE_TIME_FORMATTER)}: ${node.meta.id.value}"
    }

fun toReadableStringShort(
    node: Node
): String =
    when (node) {
        is CollectionNode -> "[${node.children.size} element${if (node.children.size == 1) "" else "s"}]"
        is Leaf -> toReadableStringShort(node.value)
        is RefNode -> node.type.name
        is SnapshotINode -> "${node.meta.timestamp.format(DATE_TIME_FORMATTER)}: ${node.meta.id.value}"
    }
