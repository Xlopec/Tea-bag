package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.screens.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.core.debug.app.Message
import io.github.xlopec.tea.core.debug.app.domain.ComponentDebugState
import io.github.xlopec.tea.core.debug.app.domain.FilterOption
import io.github.xlopec.tea.core.debug.app.domain.FilterOption.REGEX
import io.github.xlopec.tea.core.debug.app.domain.FilterOption.SUBSTRING
import io.github.xlopec.tea.core.debug.app.domain.FilterOption.WORDS
import io.github.xlopec.tea.core.debug.app.domain.Settings
import io.github.xlopec.tea.core.debug.app.feature.presentation.UpdateFilter
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.ValidatedTextField
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.toReadableStringDetailed
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.toReadableStringShort
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.tree.ProjectLocal
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.tree.Tree
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.tree.TreeFormatter
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.tree.toRenderTree
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

private val SplitPaneMinContentHeight = 100.dp

typealias MessageHandler = (Message) -> Unit

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun Component(
    project: Project,
    settings: Settings,
    state: ComponentDebugState,
    events: MessageHandler,
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
    events: MessageHandler
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
