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

/**
 * This custom tree view implementation is a temp solution, it'll be removed
 * when officially supported composable tree view implementation is released
 */
@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.mouseClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ActionIcons.Expand
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.PreviewMode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.Class
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.Property
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.Snapshot
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.toReadableString
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.toReadableStringLong
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.toReadableStringShort
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.theme.PluginPreviewTheme
import io.github.xlopec.tea.time.travel.plugin.model.BooleanWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CharWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Null
import io.github.xlopec.tea.time.travel.plugin.model.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Property
import io.github.xlopec.tea.time.travel.plugin.model.Ref
import io.github.xlopec.tea.time.travel.plugin.model.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.control.DropdownMenu

typealias TreeFormatter = (Value) -> String
typealias TreeSelectionState = MutableState<Any?>

private val LocalInitialExpandState = compositionLocalOf { false }
private val LocalTreeFormatter = compositionLocalOf<TreeFormatter> { error("TreeFormatter wasn't provided") }

@Composable
fun Tree(
    modifier: Modifier = Modifier,
    root: Value,
    formatter: TreeFormatter,
    valuePopupContent: @Composable (Value) -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        val state = rememberScrollState()
        val selection = remember { mutableStateOf<Any?>(null) }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(state)) {
            CompositionLocalProvider(LocalTreeFormatter provides formatter) {
                SubTree(root, 0, formatter(root), selection, valuePopupContent)
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

@Composable
fun Tree(
    modifier: Modifier = Modifier,
    roots: List<FilteredSnapshot>,
    formatter: TreeFormatter,
    valuePopupContent: @Composable (Value) -> Unit,
    snapshotPopupContent: @Composable (FilteredSnapshot) -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        val state = rememberScrollState()
        val selection = remember { mutableStateOf<Any?>(null) }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(state)) {
            CompositionLocalProvider(LocalTreeFormatter provides formatter) {
                roots.forEach { root ->
                    SnapshotSubTree(root, selection, valuePopupContent, snapshotPopupContent)
                }
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

@Composable
private fun SubTree(
    value: Value,
    level: Int,
    text: String,
    state: TreeSelectionState,
    valuePopupContent: @Composable (Value) -> Unit,
) = when (value) {
    is StringWrapper,
    is CharWrapper,
    is NumberWrapper,
    is BooleanWrapper,
    Null -> LeafNode(level, text, Property, value, state) { valuePopupContent(value) }
    is CollectionWrapper -> CollectionSubTree(value, level, text, state) { valuePopupContent(value) }
    is Ref -> ReferenceSubTree(level, text, value, state) { valuePopupContent(value) }
}

@Composable
private fun SnapshotSubTree(
    snapshot: FilteredSnapshot,
    state: TreeSelectionState,
    valuePopupContent: @Composable (Value) -> Unit,
    snapshotPopupContent: @Composable (FilteredSnapshot) -> Unit
) {
    val expanded = LocalInitialExpandState.current
    val expandState = remember { mutableStateOf(expanded) }
    val text = toReadableString(snapshot)

    if (snapshot.message == null && snapshot.state == null) {
        LeafNode(0, text, Snapshot, snapshot, state) { snapshotPopupContent(snapshot) }
    } else {
        ExpandableNode(0, text, Snapshot, snapshot, state, expandState) { snapshotPopupContent(snapshot) }
    }

    if (expandState.value) {
        val formatter = LocalTreeFormatter.current

        if (snapshot.message != null) {

            Text(
                modifier = Modifier.fillMaxWidth().indentLevel(1),
                text = "Message"
            )

            SubTree(snapshot.message, 1, formatter(snapshot.message), state, valuePopupContent)
        }

        if (snapshot.state != null) {
            Text(
                modifier = Modifier.fillMaxWidth().indentLevel(1),
                text = "State"
            )

            SubTree(snapshot.state, 1, formatter(snapshot.state), state, valuePopupContent)
        }

        if (snapshot.commands != null) {
            Text(
                modifier = Modifier.fillMaxWidth().indentLevel(1),
                text = "Commands"
            )

            SubTree(snapshot.commands, 1, formatter(snapshot.commands), state, valuePopupContent)
        }
    }
}

@Composable
private fun ReferenceSubTree(
    level: Int,
    text: String,
    ref: Ref,
    state: TreeSelectionState,
    valuePopupContent: @Composable (Value) -> Unit,
) {
    if (ref.properties.isEmpty()) {
        LeafNode(level, text, Class, ref, state) { valuePopupContent(ref) }
    } else {
        val expanded = LocalInitialExpandState.current
        val expandState = remember { mutableStateOf(expanded) }

        ExpandableNode(level, text, Class, ref, state, expandState) { valuePopupContent(ref) }

        if (expandState.value) {
            val formatter = LocalTreeFormatter.current

            ref.properties.forEach {
                SubTree(it.v, level + 1, "${it.name}=${formatter(it.v)}", state, valuePopupContent)
            }
        }
    }
}

@Composable
private fun CollectionSubTree(
    collection: CollectionWrapper,
    level: Int,
    text: String,
    state: TreeSelectionState,
    valuePopupContent: @Composable (Value) -> Unit,
) {
    if (collection.items.isEmpty()) {
        LeafNode(level, text, Property, collection, state) { valuePopupContent(collection) }
    } else {
        val expanded = LocalInitialExpandState.current
        val expandState = remember { mutableStateOf(expanded) }

        ExpandableNode(level, text, Property, collection, state, expandState) { valuePopupContent(collection) }

        if (expandState.value) {
            val formatter = LocalTreeFormatter.current

            collection.items.forEachIndexed { index, value ->
                SubTree(value, level + 1, "[$index] = ${formatter(value)}", state, valuePopupContent)
            }
        }
    }
}

@Composable
private fun LeafNode(
    level: Int,
    text: String,
    painter: Painter,
    node: Any,
    state: TreeSelectionState,
    popupContent: @Composable () -> Unit,
) {
    Box {
        val showPopup = remember { mutableStateOf(false) }

        DropdownMenu(
            onDismissRequest = { showPopup.value = false },
            expanded = showPopup.value
        ) {
            popupContent()
        }

        TreeRow(
            modifier = Modifier
                .fillMaxWidth()
                .indentLevel(level)
                .selected(state.value === node)
                // TODO: should handle both left and right clicks
                .mouseClickable {
                    state.value = node

                    if (buttons.isSecondaryPressed) {
                        showPopup.value = true
                    }
                },
            text = text,
            painter = painter
        )
    }
}

@Composable
private fun ExpandableNode(
    level: Int,
    text: String,
    painter: Painter,
    node: Any,
    state: TreeSelectionState,
    expandedState: MutableState<Boolean>,
    popupContent: @Composable () -> Unit,
) {
    Box {
        val showPopup = remember { mutableStateOf(false) }

        DropdownMenu(
            onDismissRequest = { showPopup.value = false },
            expanded = showPopup.value
        ) {
            popupContent()
        }

        TreeRow(
            modifier = Modifier
                .fillMaxWidth()
                .indentLevel(level)
                .selected(state.value === node)
                .mouseClickable {
                    state.value = node

                    if (buttons.isPrimaryPressed) {
                        expandedState.value = !expandedState.value
                    } else if (buttons.isSecondaryPressed) {
                        showPopup.value = true
                    }
                },
            text = text,
            painter = painter,
            leadingIcon = {
                Image(
                    modifier = Modifier.graphicsLayer(rotationZ = if (expandedState.value) 90f else 0f),
                    painter = Expand,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun TreeRow(
    modifier: Modifier,
    text: String,
    painter: Painter,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
    ) {

        Image(
            modifier = Modifier.size(16.dp),
            painter = painter,
            contentDescription = "Expandable row: $text"
        )

        Spacer(Modifier.width(4.dp))

        Row {
            leadingIcon?.invoke()
            Text(text = text)
        }
    }
}

private val IndentPadding = 12.dp

@Composable
private fun Modifier.selected(
    isSelected: Boolean
) = background(if (isSelected) JBTheme.selectionColors.active else Color.Unspecified)

private fun Modifier.indentLevel(
    level: Int,
    step: Dp = IndentPadding,
) = padding(
    start = Dp(step.value * level) + 2.dp,
    top = 2.dp,
    end = 2.dp,
    bottom = 2.dp
)

private val PreviewTreeRoot = Ref(
    Type.of("io.github.xlopec.Developer"),
    Property("name", StringWrapper("Max")),
    Property("surname", StringWrapper("Oliynick")),
    Property(
        "interests", CollectionWrapper(
            listOf(
                StringWrapper("Jetpack Compose"),
                StringWrapper("Programming"),
                StringWrapper("FP")
            )
        )
    ),
    Property("emptyCollection", CollectionWrapper())
)

@Preview
@Composable
private fun ValueTreePreviewExpandedShort() {
    PluginPreviewTheme {
        CompositionLocalProvider(
            PreviewMode provides true,
            LocalInitialExpandState provides true
        ) {
            Tree(
                root = PreviewTreeRoot,
                formatter = ::toReadableStringShort,
                valuePopupContent = {}
            )
        }
    }
}

@Preview
@Composable
private fun ValueTreePreviewExpandedLong() {
    PluginPreviewTheme {
        CompositionLocalProvider(
            PreviewMode provides true,
            LocalInitialExpandState provides true
        ) {
            Tree(
                root = PreviewTreeRoot,
                formatter = ::toReadableStringLong,
                valuePopupContent = {}
            )
        }
    }
}
