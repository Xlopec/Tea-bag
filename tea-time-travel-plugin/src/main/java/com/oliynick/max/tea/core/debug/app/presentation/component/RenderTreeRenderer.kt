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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalDesktopApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oliynick.max.tea.core.debug.app.component.resolver.appState
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

sealed interface Node : ValueTree {
    val expanded: MutableState<Boolean>
}

@JvmInline
value class Leaf1(
    val value: Value,
) : ValueTree

data class RefNode1(
    val type: Type,
    val properties: Collection<PropertyNode1>,
    override val expanded: MutableState<Boolean>,
) : Node

data class PropertyNode1(
    val name: String,
    val v: ValueTree,
)

data class CollectionNode(
    override val expanded: MutableState<Boolean>,
    val values: List<ValueTree>,
) : Node

//todo rework
fun Value.toRenderTree(expanded: Boolean = true): ValueTree =
    when (this) {
        is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper, Null -> Leaf1(this)
        is CollectionWrapper -> CollectionNode(
            mutableStateOf(expanded),
            value.map { it.toRenderTree() }
        )
        is Ref -> RefNode1(
            type,
            properties.map { PropertyNode1(it.name, it.v.toRenderTree()) },
            mutableStateOf(expanded)
        )
    }

@Composable
fun ValueTree(
    value: ValueTree,
    formatter: TreeItemFormatter,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        subtree(value, 0, formatter, formatter.format(value))
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
    treeNode: ValueTree,
    index: Int,
    level: Int,
    formatter: TreeItemFormatter,
) {
    subtree(treeNode, level, formatter, "[$index] = " + formatter.format(treeNode))
}

fun LazyListScope.collectionNode(
    node: CollectionNode,
    level: Int,
    formatter: TreeItemFormatter,
) {
    item {
        ExpandableNode(level, formatter.format(node), node.expanded)
    }

    if (node.expanded.value) {
        elementNodes(node.values, level + 1, formatter)
    }
}

private fun RowColor(
    level: Int,
) = if (level % 2 == 0) Color.Cyan else Color.Red

fun LazyListScope.propertyNode(
    node: PropertyNode1,
    level: Int,
    formatter: TreeItemFormatter,
) {
    subtree(node.v, level, formatter, "${node.name}=${formatter.format(node.v)}")
}

fun LazyListScope.subtree(
    node: ValueTree,
    level: Int,
    formatter: TreeItemFormatter,
    text: String,
) {
    when (node) {
        is RefNode1 -> {
            item {
                ExpandableNode(level, text, node.expanded)
            }

            if (node.expanded.value) {
                propertyNodes(node.properties, level + 1, formatter)
            }
        }
        is CollectionNode -> {

            item {
                ExpandableNode(level, text, node.expanded)
            }

            if (node.expanded.value) {
                elementNodes(node.values, level + 1, formatter)
            }
        }
        is Leaf1 -> {
            item {
                LeafNode(level, text) { }
            }
        }
    }
}

@OptIn(ExperimentalDesktopApi::class)
@Composable
fun LeafNode(
    level: Int,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (PreviewMode.current) Modifier.background(RowColor(level)) else Modifier)
            // TODO: should handle both left and right clicks
            .mouseClickable { buttons.isSecondaryPressed } //(onClick = { onClick() })
            .indentLevel(level),
    ) {
        Text(text = text)
    }
}

@Composable
fun ExpandableNode(
    level: Int,
    text: String,
    expanded: MutableState<Boolean>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (PreviewMode.current) Modifier.background(RowColor(level)) else Modifier)
            .clickable(onClick = { expanded.value = !expanded.value })
            .indentLevel(level),
    ) {
        Text(text = (if (expanded.value) " - " else " + ") + text)
    }
}

fun Modifier.indentLevel(
    level: Int,
    step: Dp = 24.dp,
) = padding(start = Dp(step.value * level))

private val PreviewMode = compositionLocalOf { false }

@Preview
@Composable
private fun ValueTreePreviewExpanded() {
    Surface(color = Color.Unspecified) {
        CompositionLocalProvider(PreviewMode provides true) {
            ValueTree(appState.toRenderTree(expanded = true), TreeItemFormatterImpl)
        }
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
