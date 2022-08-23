package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import arrow.core.Valid
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.UpdateServerSettings
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.Input
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.areSettingsModifiable
import io.github.xlopec.tea.time.travel.plugin.ui.control.ValidatedTextField
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import io.kanro.compose.jetbrains.control.Text

internal const val HostFieldTag = "host field"
internal const val PortFieldTag = "port field"
internal const val HostFieldPlaceholder = "Provide host"
internal const val PortFieldPlaceholder = "Provide port"

// TODO consider moving these settings to IJ Preferences
@Composable
internal fun SettingsFields(
    state: State,
    handler: MessageHandler,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val maxTextWidth = remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .then(
                        if (maxTextWidth.value == 0.dp) {
                            Modifier
                        } else {
                            Modifier.requiredWidth(maxTextWidth.value)
                        }
                    )
                    .onPlaced {
                        maxTextWidth.value = maxOf(maxTextWidth.value, with(density) { it.size.width.toDp() })
                    },
                text = "Host name:"
            )

            ValidatedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HostFieldTag),
                validated = state.debugger.settings.host,
                placeholder = HostFieldPlaceholder,
                onValueChange = { s ->
                    handler(UpdateServerSettings(host = s, port = state.debugger.settings.port.input))
                },
                enabled = state.areSettingsModifiable
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                modifier = Modifier.then(
                    if (maxTextWidth.value == 0.dp) {
                        Modifier
                    } else {
                        Modifier.requiredWidth(maxTextWidth.value)
                    }
                )
                    .onPlaced {
                        maxTextWidth.value = maxOf(maxTextWidth.value, with(density) { it.size.width.toDp() })
                    },
                text = "Port number:"
            )

            ValidatedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PortFieldTag),
                validated = state.debugger.settings.port,
                placeholder = PortFieldPlaceholder,
                onValueChange = { s ->
                    handler(UpdateServerSettings(host = state.debugger.settings.host.input, port = s))
                },
                enabled = state.areSettingsModifiable
            )
        }
    }
}

@Preview
@Composable
private fun SettingsFieldsPreview() {
    PluginPreviewTheme {
        SettingsFields(
            modifier = Modifier.fillMaxWidth(),
            state = State(
                settings = Settings(
                    host = Input("localhost", Valid(Host.newOrNull("localhost")!!)),
                    port = Input("8080", Valid(Port(8080))),
                    isDetailedOutput = false,
                    clearSnapshotsOnAttach = true,
                )
            ),
            handler = {},
        )
    }
}
