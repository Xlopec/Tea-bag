package com.oliynick.max.tea.core.debug.app.presentation.screens.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.FilterOption
import com.oliynick.max.tea.core.debug.app.domain.FilterOption.*
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.message.Message
import com.oliynick.max.tea.core.debug.app.message.UpdateFilter
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValidatedTextField
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.toReadableStringDetailed
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.toReadableStringShort
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.ProjectLocal
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.Tree
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.TreeFormatter
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.toRenderTree
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

private val SplitPaneMinContentHeight = 100.dp

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun Component(
    project: Project,
    settings: Settings,
    state: ComponentDebugState,
    events: (Message) -> Unit,
) {
    Column {

        ComponentFilterHeader(state, events)

        val splitterState = rememberSplitPaneState()
        val formatter: TreeFormatter = if (settings.isDetailedOutput) ::toReadableStringDetailed else ::toReadableStringShort

        CompositionLocalProvider(
            ProjectLocal provides project
        ) {
            VerticalSplitPane(splitPaneState = splitterState) {
                first(SplitPaneMinContentHeight) {
                    val snapshotsTree by derivedStateOf { state.filteredSnapshots.toRenderTree() }

                    Tree(
                        id = state.id,
                        modifier = Modifier.fillMaxSize().border(1.dp, Color.Black.copy(alpha = 0.60f)),
                        roots = snapshotsTree,
                        formatter = formatter,
                        handler = events,
                    )
                }

                second(SplitPaneMinContentHeight) {
                    val stateTree by derivedStateOf { state.state.toRenderTree() }

                    Tree(
                        id = state.id,
                        modifier = Modifier.fillMaxSize().border(1.dp, Color.Black.copy(alpha = 0.60f)),
                        root = stateTree,
                        formatter = formatter,
                        handler = events,
                    )
                }

                splitter {
                    visiblePart {
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colors.background)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentFilterHeader(
    state: ComponentDebugState,
    events: (Message) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        ValidatedTextField(
            validated = state.filter.predicate,
            label = "Filter",
            placeholder = "Filter properties, snapshots, etc.",
            modifier = Modifier.weight(1f).heightIn(28.dp, TextFieldDefaults.MinHeight),
            onValueChange = { s ->
                events(UpdateFilter(state, s))
            }
        )

        TextCheckbox(
            text = "Match case",
            checked = !state.filter.ignoreCase,
            onCheckedChange = { matchCase ->
                events(UpdateFilter(state, matchCase))
            }
        )

        TextCheckbox(
            text = "Regex",
            checked = state.filter.option === REGEX,
            onCheckedChange = { checked ->
                events(UpdateFilter(state, REGEX.takeIf { checked }))
            }
        )

        TextCheckbox(
            text = "Words",
            checked = state.filter.option === WORDS,
            onCheckedChange = { checked ->
                events(UpdateFilter(state, WORDS.takeIf { checked }))
            }
        )
    }
}

@Composable
fun TextCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(4.dp))
        Text(modifier = Modifier.align(Alignment.CenterVertically), text = text)
    }
}

@Preview
@Composable
fun CheckPreview() {
    Surface(color = Color.Unspecified) {
        TextCheckbox(
            "Check box sample",
            true,
            {}
        )
    }
}

private fun UpdateFilter(
    state: ComponentDebugState,
    newText: String,
) = with(state) { UpdateFilter(id, newText, filter.ignoreCase, filter.option) }

private fun UpdateFilter(
    state: ComponentDebugState,
    matchCase: Boolean,
) = with(state) { UpdateFilter(id, filter.predicate.input, !matchCase, filter.option) }

private fun UpdateFilter(
    state: ComponentDebugState,
    option: FilterOption?,
) = with(state) { UpdateFilter(id, filter.predicate.input, filter.ignoreCase, option ?: SUBSTRING) }
