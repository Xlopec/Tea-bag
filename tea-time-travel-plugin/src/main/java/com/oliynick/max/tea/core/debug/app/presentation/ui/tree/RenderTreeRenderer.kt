/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.presentation.ui.tree

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oliynick.max.tea.core.debug.app.component.resolver.appState
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValueIcon.ClassIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValueIcon.PropertyIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.toReadableStringShort

typealias TreeFormatter = (Node) -> String

// TODO: rework
object TreeItemFormatterImpl : TreeFormatter {
    override fun invoke(p: Node): String =
        when (p) {
            is CollectionNode -> p.children.joinToString(prefix = "[",
                postfix = "]",
                transform = TreeItemFormatterImpl::invoke)
            is Leaf -> toReadableStringShort(p.value)
            is RefNode -> p.type.name
        }
}

@Composable
fun Tree(
    root: Node,
    formatter: TreeFormatter,
) {
    val state = remember(root) { Tree(root) }

    Tree(state, formatter)
}

@Composable
fun Tree(
    tree: Tree,
    formatter: TreeFormatter,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        subTree(tree.root, 0, formatter, formatter(tree.root), tree)
    }
}

fun LazyListScope.subTree(
    node: Node,
    level: Int,
    formatter: TreeFormatter,
    text: String,
    state: Tree,
) =
    when (node) {
        is RefNode -> referenceSubTree(level, text, node, formatter, state)
        is CollectionNode -> collectionSubTree(node, level, text, formatter, state)
        is Leaf -> leaf(level, text, node, state)
    }

private fun LazyListScope.referenceSubTree(
    level: Int,
    text: String,
    node: RefNode,
    formatter: TreeFormatter,
    state: Tree,
) {
    item {
        ExpandableNode(level, text, ClassIconC, node.expanded)
    }

    if (node.expanded.value) {
        node.children.forEach {
            subTree(it.node, level + 1, formatter, "${it.name}=${formatter(it.node)}", state)
        }
    }
}

fun LazyListScope.collectionSubTree(
    node: CollectionNode,
    level: Int,
    text: String,
    formatter: TreeFormatter,
    state: Tree,
) {
    item {
        ExpandableNode(level, text, PropertyIconC, node.expanded)
    }

    if (node.expanded.value) {
        node.children.forEachIndexed { index, value ->
            subTree(value, level + 1, formatter, "[$index] = " + formatter(value), state)
        }
    }
}

private fun LazyListScope.leaf(
    level: Int,
    text: String,
    leaf: Leaf,
    state: Tree,
) {
    item {
        LeafNode(level, text, PropertyIconC, leaf, state) { }
    }
}

// todo: refactor LeafNode & ExpandableNode as they duplicate each other
@OptIn(ExperimentalDesktopApi::class)
@Composable
fun LeafNode(
    level: Int,
    text: String,
    image: ImageBitmap,
    leaf: Leaf,
    state: Tree,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // fixme compare parent path to compare
           // .background(if ((state.selected.value as? Leaf1)?.value === leaf.value) /*fixme: what to do for cached nodes?*/ Color.Red else Color.Unspecified)
            // TODO: should handle both left and right clicks
            .mouseClickable {
                state.selected.value = leaf
            }
            .indentLevel(level),
    ) {

        Image(
            modifier = Modifier.size(ImageSmall),
            bitmap = image,
            contentDescription = "Row: $text"
        )

        Spacer(Modifier.width(SpaceSmall))

        Text(text = text)
    }
}

@Composable
fun ExpandableNode(
    level: Int,
    text: String,
    image: ImageBitmap,
    expanded: MutableState<Boolean>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (PreviewMode.current) Modifier.background(RowColor(level)) else Modifier)
            .clickable(onClick = { expanded.value = !expanded.value })
            .indentLevel(level),
    ) {

        Image(
            modifier = Modifier.size(ImageSmall),
            bitmap = image,
            contentDescription = "Expandable row: $text"
        )

        Spacer(Modifier.width(SpaceSmall))

        Text(text = (if (expanded.value) "- " else "+ ") + text)
    }
}

val SpaceSmall = 4.dp

val PaddingSmall = 2.dp

val ImageSmall = 16.dp

private val IndentPadding = 12.dp

fun Modifier.indentLevel(
    level: Int,
    step: Dp = IndentPadding,
) = padding(
    start = Dp(step.value * level) + PaddingSmall,
    top = PaddingSmall,
    end = PaddingSmall,
    bottom = PaddingSmall
)

private fun RowColor(
    level: Int,
) = if (level % 2 == 0) Color.Cyan else Color.Red

private val PreviewMode = compositionLocalOf { false }

@Preview
@Composable
private fun ValueTreePreviewExpanded() {
    Surface(color = Color.Unspecified) {
        CompositionLocalProvider(PreviewMode provides false) {
            Tree(Tree(appState.toRenderTree(expanded = true)), TreeItemFormatterImpl)
        }
    }
}