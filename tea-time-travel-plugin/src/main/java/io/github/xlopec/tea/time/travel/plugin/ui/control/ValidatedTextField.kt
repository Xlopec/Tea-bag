package io.github.xlopec.tea.time.travel.plugin.ui.control

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import arrow.core.Valid
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.Input
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.ui.ActionsMenu
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import io.kanro.compose.jetbrains.control.JPanel
import io.kanro.compose.jetbrains.control.Text
import io.kanro.compose.jetbrains.control.TextField

private val PreviewSettings = Settings(
    Input("192.168.1.1", Valid(Host.newOrNull("192.168.1.1")!!)),
    Input("8080", Valid(Port(8080))),
    isDetailedOutput = false,
    clearSnapshotsOnAttach = true
)

@Preview
@Composable
fun BottomActionMenuPreview() {
    PluginPreviewTheme {
        JPanel {
            ActionsMenu(
                modifier = Modifier.fillMaxWidth(),
                onImportSession = {},
                onExportSession = {},
                onServerAction = {},
                onSettingsAction = {},
                state = State(PreviewSettings),
            )
        }
    }
}

@Composable
fun ValidatedTextField(
    validated: Input<String, *>,
    placeholder: String,
    modifier: Modifier = Modifier,
    onValueChange: (newValue: String) -> Unit,
    enabled: Boolean = true,
) {
    TextField(
        modifier = modifier,
        value = validated.input,
        onValueChange = { onValueChange(it) },
        placeholder = { Text(text = placeholder) },
        isError = validated.value.isInvalid,
        singleLine = true,
        enabled = enabled
    )
}

@Preview
@Composable
private fun ValidatedTextFieldPreview() {
    PluginPreviewTheme {
        JPanel(modifier = Modifier.fillMaxSize()) {
            ValidatedTextField(
                validated = Input("text field input", Valid("input")),
                placeholder = "Placeholder",
                modifier = Modifier,
                onValueChange = {}
            )
        }
    }
}
