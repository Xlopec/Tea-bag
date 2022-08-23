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
@file:OptIn(ExperimentalFoundationApi::class) @file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ActionIcons.Expand
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ValueIcon.Class
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ValueIcon.Property
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ValueIcon.Snapshot
import io.github.xlopec.tea.time.travel.plugin.util.clickable
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.LocalTypography
import io.kanro.compose.jetbrains.control.DropdownMenu
import io.kanro.compose.jetbrains.control.Text

typealias TreeFormatter = (Value) -> String
typealias TreeSelectionState = MutableState<Any?>

internal fun Tag(
    value: Value
) = "value: ${value.stringValue}"

internal fun Tag(
    meta: SnapshotMeta
) = "filtered snapshot: ${meta.id}/${meta.timestamp}"

internal val LocalInitialExpandState = compositionLocalOf { false }
private val LocalTreeFormatter = compositionLocalOf<TreeFormatter> { error("TreeFormatter wasn't provided") }

/**
 * Additional `y` offset so that when user opens DropDown menu the top most item isn't hovered by pointer input
 */
private val PointerCaptureInputAvoidanceOffset = DpOffset(0.dp, 3.dp)

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
                Text(
                    modifier = Modifier.padding(all = 4.dp), style = LocalTypography.current.defaultBold, text = "State"
                )
                SubTree(root, 0, formatter(root), selection, valuePopupContent)
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = state)
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
                Text(
                    modifier = Modifier.padding(all = 4.dp),
                    style = LocalTypography.current.defaultBold,
                    text = "Snapshots"
                )

                roots.forEach { root ->
                    SnapshotSubTree(root, selection, valuePopupContent, snapshotPopupContent)
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(), adapter = rememberScrollbarAdapter(
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
    is StringWrapper, is CharWrapper, is NumberWrapper, is BooleanWrapper, Null -> LeafNode(
        modifier = Modifier.fillMaxWidth().testTag(Tag(value)),
        level = level,
        text = text,
        painter = Property,
        node = value,
        state = state,
        popupContent = { valuePopupContent(value) }
    )
    is CollectionWrapper -> CollectionSubTree(value, level, text, state, valuePopupContent)
    is Ref -> ReferenceSubTree(level, text, value, state, valuePopupContent)
}

@Composable
private fun SnapshotSubTree(
    snapshot: FilteredSnapshot,
    state: TreeSelectionState,
    valuePopupContent: @Composable (Value) -> Unit,
    snapshotPopupContent: @Composable (FilteredSnapshot) -> Unit,
) {
    val expanded = LocalInitialExpandState.current
    val expandState = remember { mutableStateOf(expanded) }
    val text = toReadableString(snapshot)

    if (snapshot.message == null && snapshot.state == null) {
        LeafNode(
            modifier = Modifier.fillMaxWidth().testTag(Tag(snapshot.meta)),
            level = 0,
            text = text,
            painter = Snapshot,
            node = snapshot,
            state = state,
            popupContent = { snapshotPopupContent(snapshot) }
        )
    } else {
        ExpandableNode(
            modifier = Modifier.fillMaxWidth().testTag(Tag(snapshot.meta)),
            level = 0,
            text = text,
            painter = Snapshot,
            node = snapshot,
            state = state,
            expandedState = expandState,
            popupContent = { snapshotPopupContent(snapshot) }
        )
    }

    if (expandState.value) {
        val formatter = LocalTreeFormatter.current

        if (snapshot.message != null) {

            Text(
                style = LocalTypography.current.defaultBold,
                modifier = Modifier.fillMaxWidth().indentLevel(1),
                text = "Message"
            )

            SubTree(snapshot.message, 1, formatter(snapshot.message), state, valuePopupContent)
        }

        if (snapshot.state != null) {
            Text(
                style = LocalTypography.current.defaultBold,
                modifier = Modifier.fillMaxWidth().indentLevel(1),
                text = "State"
            )

            SubTree(snapshot.state, 1, formatter(snapshot.state), state, valuePopupContent)
        }

        if (snapshot.commands != null) {
            Text(
                style = LocalTypography.current.defaultBold,
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
        LeafNode(
            modifier = Modifier.fillMaxWidth().testTag(Tag(ref)),
            level = level,
            text = text,
            painter = Class,
            node = ref,
            state = state,
            popupContent = { valuePopupContent(ref) }
        )
    } else {
        val expanded = LocalInitialExpandState.current
        val expandState = remember { mutableStateOf(expanded) }

        ExpandableNode(
            modifier = Modifier.fillMaxWidth().testTag(Tag(ref)),
            level = level,
            text = text,
            painter = Class,
            node = ref,
            state = state,
            expandedState = expandState,
            popupContent = { valuePopupContent(ref) }
        )

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
        LeafNode(
            modifier = Modifier.fillMaxWidth().testTag(Tag(collection)),
            level = level,
            text = text,
            painter = Property,
            node = collection,
            state = state,
            popupContent = { valuePopupContent(collection) }
        )
    } else {
        val expanded = LocalInitialExpandState.current
        val expandState = remember { mutableStateOf(expanded) }

        ExpandableNode(
            modifier = Modifier.fillMaxWidth().testTag(Tag(collection)),
            level = level,
            text = text,
            painter = Property,
            node = collection,
            state = state,
            expandedState = expandState,
            popupContent = { valuePopupContent(collection) }
        )

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
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val showPopup = remember { mutableStateOf(false) }
        val offset = remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

        DropdownMenu(
            onDismissRequest = { showPopup.value = false },
            expanded = showPopup.value,
            offset = offset.value,
        ) {
            popupContent()
        }

        TreeRow(
            modifier = Modifier
                .fillMaxWidth()
                .selected(state.value === node)
                .indentLevel(level)
                // TODO: should handle both left and right clicks
                .clickable { _, upOffset ->
                    state.value = node

                    if (buttons.isSecondaryPressed) {
                        offset.value = upOffset + PointerCaptureInputAvoidanceOffset
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
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val showPopup = remember { mutableStateOf(false) }
        val offset = remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

        DropdownMenu(
            onDismissRequest = { showPopup.value = false },
            expanded = showPopup.value,
            offset = offset.value,
        ) {
            popupContent()
        }

        TreeRow(
            modifier = Modifier
                .fillMaxWidth()
                .selected(state.value === node)
                .indentLevel(level)
                .clickable { _, upOffset ->
                    state.value = node

                    if (buttons.isPrimaryPressed) {
                        expandedState.value = !expandedState.value
                    } else if (buttons.isSecondaryPressed) {
                        offset.value = upOffset + PointerCaptureInputAvoidanceOffset
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
            })
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
            contentDescription = null
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
    isSelected: Boolean,
) = background(if (isSelected) JBTheme.selectionColors.active else Color.Unspecified)

private fun Modifier.indentLevel(
    level: Int,
    step: Dp = IndentPadding,
) = padding(
    start = Dp(step.value * level) + 4.dp, top = 2.dp, end = 4.dp, bottom = 2.dp
)
