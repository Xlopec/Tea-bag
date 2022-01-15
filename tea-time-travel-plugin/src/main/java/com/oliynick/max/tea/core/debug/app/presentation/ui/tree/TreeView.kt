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

@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.presentation.ui.tree

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.component.resolver.appState
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.CloseDefaultIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ActionIcons.UpdateRunningAppIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ImageSmall
import com.oliynick.max.tea.core.debug.app.presentation.ui.PaddingSmall
import com.oliynick.max.tea.core.debug.app.presentation.ui.PreviewMode
import com.oliynick.max.tea.core.debug.app.presentation.ui.SpaceSmall
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValueIcon.ClassIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValueIcon.PropertyIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValueIcon.WatchIconC
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.DATE_TIME_FORMATTER
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.toReadableStringShort

typealias TreeFormatter = (Node) -> String

// TODO: rework
object TreeItemFormatterImpl : TreeFormatter {
    override fun invoke(p: Node): String =
        when (p) {
            is CollectionNode -> p.children.joinToString(
                prefix = "[",
                postfix = "]",
                transform = TreeItemFormatterImpl::invoke
            )
            is Leaf -> toReadableStringShort(p.value)
            is RefNode -> p.type.name
            is SnapshotINode -> "${p.meta.timestamp.format(DATE_TIME_FORMATTER)}: ${p.meta.id.value}"
        }
}

@Composable
fun Tree(
    modifier: Modifier = Modifier,
    roots: List<Node>,
    formatter: TreeFormatter,
    handler: (PluginMessage) -> Unit,
) {
    val state = remember(roots) { TreeState(roots) }

    Tree(modifier, state, formatter, handler)
}

@Composable
fun Tree(
    modifier: Modifier = Modifier,
    root: Node,
    formatter: TreeFormatter,
    handler: (PluginMessage) -> Unit,
) {
    val state = remember(root) { TreeState(root) }

    Tree(modifier, state, formatter, handler)
}

@Composable
fun Tree(
    modifier: Modifier = Modifier,
    tree: TreeState,
    formatter: TreeFormatter,
    handler: (PluginMessage) -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        val state = rememberLazyListState()

        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            tree.roots.forEach { root ->
                subTree(root, 0, formatter, formatter(root), tree, handler)
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
    }
}

private fun LazyListScope.subTree(
    node: Node,
    level: Int,
    formatter: TreeFormatter,
    text: String,
    state: TreeState,
    handler: (PluginMessage) -> Unit
) =
    when (node) {
        is SnapshotINode -> snapshotSubTree(level, formatter(node), formatter, node, state, handler)
        is RefNode -> referenceSubTree(level, text, node, formatter, state, handler)
        is CollectionNode -> collectionSubTree(node, level, text, formatter, state, handler)
        is Leaf -> leaf(level, text, node, state)
    }

private fun LazyListScope.snapshotSubTree(
    level: Int,
    text: String,
    formatter: TreeFormatter,
    node: SnapshotINode,
    state: TreeState,
    handler: (PluginMessage) -> Unit
) {
    item {
        if (node.message == null && node.state == null) {
            LeafNode(level, text, WatchIconC, node, state) { }
        } else {
            ExpandableNode(level, text, WatchIconC, node, state, handler)
        }
    }

    if (node.expanded.value) {
        if (node.message != null) {
            subTree(node.message, level + 1, formatter, "Message", state, handler)
        }

        if (node.state != null) {
            subTree(node.state, level + 1, formatter, "State", state, handler)
        }
    }
}

private fun LazyListScope.referenceSubTree(
    level: Int,
    text: String,
    node: RefNode,
    formatter: TreeFormatter,
    state: TreeState,
    handler: (PluginMessage) -> Unit,
) {
    item {
        ExpandableNode(level, text, ClassIconC, node, state, handler)
    }

    if (node.expanded.value) {
        node.children.forEach {
            subTree(it.node, level + 1, formatter, "${it.name}=${formatter(it.node)}", state, handler)
        }
    }
}

private fun LazyListScope.collectionSubTree(
    node: CollectionNode,
    level: Int,
    text: String,
    formatter: TreeFormatter,
    state: TreeState,
    handler: (PluginMessage) -> Unit
) {
    item {
        ExpandableNode(level, text, PropertyIconC, node, state, handler)
    }

    if (node.expanded.value) {
        node.children.forEachIndexed { index, value ->
            subTree(value, level + 1, formatter, "[$index] = " + formatter(value), state, handler)
        }
    }
}

private fun LazyListScope.leaf(
    level: Int,
    text: String,
    leaf: Leaf,
    state: TreeState,
) {
    item {
        LeafNode(level, text, PropertyIconC, leaf, state) { }
    }
}

// todo: refactor LeafNode & ExpandableNode as they duplicate each other
@Composable
private fun LeafNode(
    level: Int,
    text: String,
    image: ImageBitmap,
    leaf: Node,
    state: TreeState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selected(state.selected.value?.id == leaf.id)
            // TODO: should handle both left and right clicks
            .mouseClickable {
                state.selected.value = leaf
                onClick()
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
private fun ExpandableNode(
    level: Int,
    text: String,
    image: ImageBitmap,
    node: INode,
    state: TreeState,
    handler: (PluginMessage) -> Unit
) {
    val showPopup = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selected(state.selected.value?.id == node.id)
            .mouseClickable {
                state.selected.value = node

                if (buttons.isPrimaryPressed) {
                    node.expanded.value = !node.expanded.value
                } else if (buttons.isSecondaryPressed) {
                    showPopup.value = true
                }
            }
            .indentLevel(level),
    ) {

        Image(
            modifier = Modifier.size(ImageSmall),
            bitmap = image,
            contentDescription = "Expandable row: $text"
        )

        Spacer(Modifier.width(SpaceSmall))

        Text(text = (if (node.expanded.value) "- " else "+ ") + text)

        if (showPopup.value) {
            // fixme move popup to a separate file, add slot API for it. Do just the same for clicks and etc.
            ActionsPopup(
                onDismiss = { showPopup.value = false },
                handler = handler
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ActionsPopup(
    onDismiss: () -> Unit,
    handler: (PluginMessage) -> Unit
) {
    Popup(onDismissRequest = onDismiss) {
        Surface(elevation = 8.dp) {
            Column {
                PopupItem(CloseDefaultIconC, "Delete All") { }
                PopupItem(UpdateRunningAppIconC, "Reset to This") {
                }
                PopupItem(UpdateRunningAppIconC, "Apply this message") {
                }
                PopupItem(UpdateRunningAppIconC, "Apply this state") {
                }
            }
        }
    }
}

@Composable
fun PopupItem(
    image: ImageBitmap,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Image(
            modifier = Modifier.size(ImageSmall),
            bitmap = image,
            contentDescription = text
        )

        Spacer(Modifier.width(SpaceSmall))

        Text(text = text)
    }
}

private val IndentPadding = 12.dp

@Composable
private fun Modifier.selected(
    isSelected: Boolean
) = background(if (isSelected) LocalContentColor.current.copy(alpha = 0.60f) else Color.Unspecified)

private fun Modifier.indentLevel(
    level: Int,
    step: Dp = IndentPadding,
) = padding(
    start = Dp(step.value * level) + PaddingSmall,
    top = PaddingSmall,
    end = PaddingSmall,
    bottom = PaddingSmall
)

@Preview
@Composable
fun ValueTreePreviewExpanded() {
    Surface(color = Color.Unspecified) {
        CompositionLocalProvider(PreviewMode provides true) {
            Tree(root = appState.toRenderTree(expanded = true), formatter = TreeItemFormatterImpl) {}
        }
    }
}