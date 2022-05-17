package io.github.xlopec.tea.time.travel.plugin.ui.control

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.Valid
import io.github.xlopec.tea.time.travel.plugin.model.Validated
import io.github.xlopec.tea.time.travel.plugin.model.isValid
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.ui.BottomActionMenu
import io.kanro.compose.jetbrains.control.JPanel
import io.kanro.compose.jetbrains.control.TextField

private val PreviewSettings = Settings(Valid("192.168.1.1", Host.of("192.168.1.1")!!), Valid("8080", Port(8080)), isDetailedOutput = false)

@Preview
@Composable
fun BottomActionMenuPreview() {
    PluginPreviewTheme {
        JPanel {
            BottomActionMenu(
                onImportSession = {},
                onExportSession = {},
                state = Stopped(PreviewSettings),
                events = {}
            )
        }
    }
}

@Composable
fun ValidatedTextField(
    validated: Validated<*>,
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
        isError = !validated.isValid(),
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
                validated = Valid("text field input", "input"),
                placeholder = "Placeholder",
                modifier = Modifier,
                onValueChange = {}
            )
        }
    }
}
