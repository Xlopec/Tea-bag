package com.oliynick.max.tea.core.debug.app.presentation.screens.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.component.cms.UpdateFilter
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.FilterOption
import com.oliynick.max.tea.core.debug.app.domain.FilterOption.*
import com.oliynick.max.tea.core.debug.app.presentation.ui.ValidatedTextField
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.Tree
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.TreeItemFormatterImpl
import com.oliynick.max.tea.core.debug.app.presentation.ui.tree.toRenderTree

@Composable
fun Component(
    project: Project,
    state: ComponentDebugState,
    events: (PluginMessage) -> Unit,
) {

    ComponentFilterHeader(state, events)

    val treeState by derivedStateOf { state.filteredSnapshots.toRenderTree() }

    Tree(treeState, TreeItemFormatterImpl) { message ->
        println("Event $message")
    }
}

@Composable
private fun ComponentFilterHeader(
    state: ComponentDebugState,
    events: (PluginMessage) -> Unit
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
