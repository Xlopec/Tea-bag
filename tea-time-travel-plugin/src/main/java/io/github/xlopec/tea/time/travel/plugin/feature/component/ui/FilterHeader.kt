package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.UpdateFilter
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.Filter
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.FilterOption
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.FilterOption.REGEX
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.FilterOption.WORDS
import io.github.xlopec.tea.time.travel.plugin.ui.control.ValidatedTextField
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.kanro.compose.jetbrains.control.CheckBox
import io.kanro.compose.jetbrains.control.JPanel
import io.kanro.compose.jetbrains.control.Text

@Composable
internal fun FiltersHeader(
    modifier: Modifier,
    id: ComponentId,
    filter: Filter,
    events: MessageHandler
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ValidatedTextField(
            validated = filter.predicate,
            placeholder = "Filter properties, snapshots, etc.",
            modifier = Modifier.weight(1f),
            onValueChange = { s ->
                events(UpdateFilter(id, filter, s))
            }
        )

        TextCheckbox(
            text = "Match case",
            checked = !filter.ignoreCase,
            onCheckedChange = { matchCase ->
                events(UpdateFilter(id, filter, matchCase))
            }
        )

        TextCheckbox(
            text = "Regex",
            checked = filter.option === REGEX,
            onCheckedChange = { checked ->
                events(UpdateFilter(id, filter, REGEX.takeIf { checked }))
            }
        )

        TextCheckbox(
            text = "Words",
            checked = filter.option === WORDS,
            onCheckedChange = { checked ->
                events(UpdateFilter(id, filter, WORDS.takeIf { checked }))
            }
        )
    }
}

@Preview
@Composable
private fun FiltersHeaderPreview() {
    PluginPreviewTheme {
        JPanel {
            FiltersHeader(
                modifier = Modifier.padding(4.dp),
                id = ComponentId("Preview"),
                filter = Filter.new("abc", REGEX, ignoreCase = true),
                events = {}
            )
        }
    }
}

@Composable
private fun TextCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        CheckBox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(4.dp))
        Text(text = text)
    }
}

private fun UpdateFilter(
    id: ComponentId,
    filter: Filter,
    newText: String,
): UpdateFilter = UpdateFilter(id, newText, filter.ignoreCase, filter.option)

private fun UpdateFilter(
    id: ComponentId,
    filter: Filter,
    matchCase: Boolean,
): UpdateFilter = UpdateFilter(id, filter.predicate.input, !matchCase, filter.option)

private fun UpdateFilter(
    id: ComponentId,
    filter: Filter,
    option: FilterOption?,
): UpdateFilter = UpdateFilter(id, filter.predicate.input, filter.ignoreCase, option ?: FilterOption.SUBSTRING)
