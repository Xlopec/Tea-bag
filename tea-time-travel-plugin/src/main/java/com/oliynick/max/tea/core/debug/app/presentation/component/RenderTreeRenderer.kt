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

package com.oliynick.max.tea.core.debug.app.presentation.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.*
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeModel

class RenderTreeRenderer private constructor(
    var formatter: ValueFormatter,
    private val transformer: RenderTree.(TreeModel, ValueFormatter) -> String,
) : TreeCellRenderer {

    companion object {

        fun SnapshotsRenderer(
            formatter: ValueFormatter,
        ) = RenderTreeRenderer(formatter, ::toReadableSnapshotString)

        fun StateRenderer(
            formatter: ValueFormatter,
        ) = RenderTreeRenderer(formatter) { _, f -> toReadableStateString(this, f) }
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component =
        JLabel().apply {

            val payload = (value as DefaultMutableTreeNode).userObject as RenderTree

            text = transformer(payload, tree.model, formatter)
            icon = payload.icon
        }

}

interface TreeItemFormatter {
    fun format(p: ValueTree): String
    fun format(p: PropertyNode1): String
}

object TreeItemFormatterImpl : TreeItemFormatter {

    override fun format(p: ValueTree): String =
        when (p) {
            is CollectionNode -> p.values.joinToString(prefix = "[",
                postfix = "]",
                transform = ::format)
            is Leaf1 -> toReadableStringShort(p.value)
            is RefNode1 -> p.type.name
        }

    override fun format(p: PropertyNode1): String = "${p.name}=${format(p.v)}"
}

sealed interface ValueTree

@JvmInline
value class Leaf1(
    val value: Value,
) : ValueTree

data class RefNode1(
    val type: Type,
    val properties: Collection<PropertyNode1>,
    val expanded: Boolean,
) : ValueTree

data class PropertyNode1(
    val name: String,
    val v: ValueTree,
)

data class CollectionNode(
    val expanded: Boolean,
    val values: List<ValueTree>,
) : ValueTree

//todo rework
fun Value.toRenderTree(): ValueTree =
    when (this) {
        is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper, Null -> Leaf1(this)
        is CollectionWrapper -> CollectionNode(
            true,
            value.map { it.toRenderTree() }
        )
        is Ref -> RefNode1(
            type,
            properties.map { PropertyNode1(it.name, it.v.toRenderTree()) },
            true
        )
    }

@Composable
fun ValueTree(
    value: ValueTree,
    formatter: TreeItemFormatter,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when (value) {
            is RefNode1 -> referenceNode(value, 0, formatter)
            is CollectionNode -> collectionNode(value, 0, formatter)
            is Leaf1 -> leaf(value, formatter)
        }
    }
}

fun LazyListScope.leaf(
    leaf: Leaf1,
    transformer: TreeItemFormatter,
) {
    item {
        Text(transformer.format(leaf))
    }
}

fun LazyListScope.propertyNodes(
    properties: Iterable<PropertyNode1>,
    level: Int,
    transformer: TreeItemFormatter,
) = properties.forEach { property -> propertyNode(property, level, transformer) }

fun LazyListScope.elementNodes(
    nodes: Iterable<ValueTree>,
    level: Int,
    formatter: TreeItemFormatter,
) = nodes.forEachIndexed { index, value -> elementNode(value, index, level, formatter) }

fun LazyListScope.elementNode(
    v: ValueTree,
    index: Int,
    level: Int,
    formatter: TreeItemFormatter,
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    when (v) {
        is RefNode1 -> {
            item {
                Text(modifier = m, text = "[$index] = " + formatter.format(v))
            }
            // expanded = true
            // is ref.expanded == true
            if (v.expanded) {
                propertyNodes(v.properties, level + 1, formatter)
            }
        }
        is CollectionNode -> {
            item {
                Text(modifier = m, text = "[$index] = " + formatter.format(v))
            }
            // expanded = true
            // is collection.expanded == true
            if (v.expanded) {
                elementNodes(v.values, level + 1, formatter)
            }
        }
        is Leaf1 -> item {
            Text(modifier = m, text = "[$index] = " + formatter.format(v))
        }
    }
}

fun LazyListScope.collectionNode(
    node: CollectionNode,
    level: Int,
    formatter: TreeItemFormatter,
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    item {
        Text(modifier = m, text = " + " + formatter.format(node))
    }

    if (node.expanded) {
        elementNodes(node.values, level + 1, formatter)
    }
}

fun LazyListScope.propertyNode(
    node: PropertyNode1,
    level: Int,
    formatter: TreeItemFormatter,
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    when (val tree = node.v) {
        is RefNode1 -> {
            item {
                Text(modifier = m, text = " + " + formatter.format(tree))
            }
            // expanded = true
            // is ref.expanded == true
            if (tree.expanded) {
                propertyNodes(tree.properties, level + 1, formatter)
            }
        }
        is CollectionNode -> {
            item {
                Text(modifier = m, text = " + " + formatter.format(node))
            }
            if (tree.expanded) {
                elementNodes(tree.values, level + 1, formatter)
            }
        }
        is Leaf1 -> item {
            Text(modifier = m, text = formatter.format(node))
        }
    }
}

fun LazyListScope.referenceNode(
    node: RefNode1,
    level: Int,
    transformer: TreeItemFormatter,
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    item {
        Text(
            modifier = m,
            text = " + " + transformer.format(node)
        )
    }

    if (node.expanded) {
        propertyNodes(node.properties, level + 1, transformer)
    }
}

private fun toReadableStateString(
    renderTree: RenderTree,
    formatter: ValueFormatter,
): String =
    when (renderTree) {
        RootNode -> "State"
        is SnapshotNode, is MessageNode, is StateNode -> error("Can't render $renderTree")
        is PropertyNode -> renderTree.toReadableString(formatter)
        is ValueNode -> renderTree.toReadableString(formatter)
        is IndexedNode -> renderTree.toReadableString(formatter)
        is EntryKeyNode -> renderTree.toReadableString(formatter)
        is EntryValueNode -> renderTree.toReadableString(formatter)
    }

private fun toReadableSnapshotString(
    renderTree: RenderTree,
    model: TreeModel,
    formatter: ValueFormatter,
): String =
    when (renderTree) {
        RootNode -> "Snapshots (${model.getChildCount(model.root)})"
        is SnapshotNode -> renderTree.snapshot.toReadableString()
        is MessageNode -> "Message"
        is StateNode -> "State"
        is PropertyNode -> renderTree.toReadableString(formatter)
        is ValueNode -> renderTree.toReadableString(formatter)
        is IndexedNode -> renderTree.toReadableString(formatter)
        is EntryKeyNode -> renderTree.toReadableString(formatter)
        is EntryValueNode -> renderTree.toReadableString(formatter)
    }
