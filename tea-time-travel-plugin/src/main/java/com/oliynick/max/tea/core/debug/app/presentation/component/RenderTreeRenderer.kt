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

interface ItemFormatter {
    fun format(v: Value) : String
    fun format(p: Property): String
}

sealed interface RenderNode1

@JvmInline
value class Leaf1(val value: Value) : RenderNode1

data class RefNode1(val ref: Ref, val expanded: Boolean) : RenderNode1

data class PropertyNode1(val p: Property, val expanded: Boolean) : RenderNode1

@Composable
fun drawComposeTreeInit(
    tree: Value,
    transformer: ItemFormatter,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        drawComposeTree(tree, 0, transformer)
    }
}

fun LazyListScope.drawComposeTree(
    tree: Value,
    level: Int,
    transformer: ItemFormatter,
) {
    when (tree) {
        is Ref -> ref(tree, level, transformer)
        is CollectionWrapper -> list(tree, level, transformer)
        is BooleanWrapper, is CharWrapper, Null, is NumberWrapper, is StringWrapper -> item {
            Text(transformer.format(tree))
        }
    }
}

fun LazyListScope.drawProperties(
    p: Iterable<Property>,
    level: Int,
    transformer: ItemFormatter
) {
    // expanded = true
    // is ref.expanded == true
    p.forEach { property ->

        // property.expanded == false
        /*item {

            Text(text = toReadableString(property))
        }*/
        // property.expanded == true

        drawProperty(property, level, transformer)
    }
}

fun LazyListScope.drawElement(
    v: Value,
    index: Int,
    level: Int,
    formatter: ItemFormatter
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    when (v) {
        is Ref -> {
            item {
                Text(modifier = m, text = "[$index] = " + formatter.format(v))
            }
            // expanded = true
            // is ref.expanded == true
            drawProperties(v.properties, level + 1, formatter)
        }
        is CollectionWrapper -> {
            item {
                Text(modifier = m, text = "[$index] = " + formatter.format(v))
            }

            // expanded = true
            // is collection.expanded == true

            v.value.forEachIndexed { index, value ->
                drawElement(value, index, level + 1, formatter)
            }
        }
        is BooleanWrapper, is CharWrapper, Null, is NumberWrapper, is StringWrapper -> item {
            Text(modifier = m, text = "[$index] = " + formatter.format(v))
        }
    }
}

fun LazyListScope.list(
    w: CollectionWrapper,
    level: Int,
    formatter: ItemFormatter
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    item {
        Text(modifier = m, text = " + " + formatter.format(w))
    }

    // expanded = true
    // is collection.expanded == true

    w.value.forEachIndexed { index, value ->
        drawElement(value, index, level + 1, formatter)
    }
}

fun LazyListScope.drawProperty(
    p: Property,
    level: Int,
    formatter: ItemFormatter
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    when (val tree = p.v) {
        is Ref -> {
            item {
                Text(modifier = m, text = " + " + formatter.format(p))
            }
            // expanded = true
            // is ref.expanded == true
            drawProperties(tree.properties, level + 1, formatter)
        }
        is CollectionWrapper -> {
            item {
                Text(modifier = m, text = " + " + formatter.format(p))
            }

            // expanded = true
            // is collection.expanded == true

            tree.value.forEachIndexed { index, value ->
                drawElement(value, index, level + 1, formatter)
            }
        }
        is BooleanWrapper, is CharWrapper, Null, is NumberWrapper, is StringWrapper -> item {
            Text(modifier = m, text = formatter.format(p))
        }
    }
}

fun LazyListScope.ref(
    tree: Ref,
    level: Int,
    transformer: ItemFormatter,
) {
    val m = Modifier.fillMaxWidth().padding(start = Dp(24f * level))

    item {
        Text(
            modifier = m,
            text = " + " + transformer.format(tree)
        )
    }

    drawProperties(tree.properties, level + 1, transformer)
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
