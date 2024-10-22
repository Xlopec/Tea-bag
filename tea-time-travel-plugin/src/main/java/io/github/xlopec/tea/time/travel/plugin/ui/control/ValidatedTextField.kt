package io.github.xlopec.tea.time.travel.plugin.ui.control

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import arrow.core.Valid
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.Input
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.ui.ActionsMenu
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

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
        Box {
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
    val state = remember { TextFieldState(validated.input) }

    LaunchedEffect(state.text) {
        onValueChange(state.text.toString())
    }

    TextField(
        modifier = modifier,
        state = state,
        outline = if (validated.value.isInvalid) Outline.Error else Outline.None,
        placeholder = { Text(text = placeholder) },
        enabled = enabled
    )
}

@Preview
@Composable
private fun ValidatedTextFieldPreview() {
    PluginPreviewTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ValidatedTextField(
                validated = Input("text field input", Valid("input")),
                placeholder = "Placeholder",
                modifier = Modifier,
                onValueChange = {}
            )
        }
    }
}
