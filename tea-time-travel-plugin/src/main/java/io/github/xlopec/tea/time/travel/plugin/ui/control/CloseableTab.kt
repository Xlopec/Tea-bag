package io.github.xlopec.tea.time.travel.plugin.ui.control

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ActionIcons
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import io.kanro.compose.jetbrains.control.ActionButton
import io.kanro.compose.jetbrains.control.JPanel
import io.kanro.compose.jetbrains.control.Tab
import io.kanro.compose.jetbrains.control.Text

@Composable
internal fun CloseableTab(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tab(
        modifier = modifier,
        selected = selected,
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(text)

            ActionButton(
                onClick = onClose
            ) {
                Image(
                    painter = ActionIcons.Close,
                    contentDescription = "Close tab"
                )
            }
        }
    }
}

@Preview
@Composable
private fun TabsPreview() {
    PluginPreviewTheme {
        JPanel(modifier = Modifier.fillMaxSize()) {
            Row {
                repeat(5) { index ->
                    CloseableTab(
                        text = "Tab #$index",
                        selected = index % 2 == 0,
                        onClose = {},
                        onSelect = {}
                    )
                }
            }
        }
    }
}
