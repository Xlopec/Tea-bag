package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
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
import kotlin.math.roundToInt

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
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
) {
    val density = LocalDensity.current
    val horizontalSpacingPx = with(density) { horizontalSpacing.toPx() }.roundToInt()
    val verticalSpacingPx = with(density) { verticalSpacing.toPx() }.roundToInt()

    Layout(
        modifier = modifier,
        content = {
            Text(
                text = "Host name:"
            )

            Text(
                text = "Port number:"
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
        },
    ) { measurables, constraints ->
        // ask for a preferred size
        val childConstraints = constraints.copy(minWidth = 0)
        val hostText = measurables[0].measure(childConstraints)
        val portText = measurables[1].measure(childConstraints)

        val firstColumnWidth = maxOf(hostText.width, portText.width)

        val inputsConstraints = childConstraints.copy(
            maxWidth = (constraints.maxWidth - firstColumnWidth - horizontalSpacingPx).coerceAtLeast(0)
        )
        val hostInput = measurables[2].measure(inputsConstraints)
        val portInput = measurables[3].measure(inputsConstraints)
        val firstRowHeight = maxOf(hostText.height, hostInput.height)
        val secondRowHeight = maxOf(portInput.height, portInput.height)
        val secondColumnWidth = maxOf(hostInput.width, portInput.width)

        val layoutWidth = firstColumnWidth + secondColumnWidth + horizontalSpacingPx
        val layoutHeight = firstRowHeight + secondRowHeight + verticalSpacingPx

        layout(layoutWidth, layoutHeight) {
            hostText.placeRelative(
                x = 0,
                y = (firstRowHeight - hostText.height) / 2
            )
            hostInput.placeRelative(
                x = firstColumnWidth + horizontalSpacingPx,
                y = (firstRowHeight - hostInput.height) / 2
            )
            portText.placeRelative(
                x = 0,
                y = verticalSpacingPx + firstRowHeight + (secondRowHeight - portText.height) / 2
            )
            portInput.placeRelative(
                x = firstColumnWidth + horizontalSpacingPx,
                y = verticalSpacingPx + firstRowHeight + (secondRowHeight - hostInput.height) / 2
            )
        }
    }
}

@Preview
@Composable
private fun SettingsFieldsPreview() {
    PluginPreviewTheme {
        SettingsFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            state = State(
                settings = Settings(
                    host = Input("localhost", Valid(Host.newOrNull("localhost")!!)),
                    port = Input("8080", Valid(Port(8080))),
                    isDetailedOutput = false,
                    clearSnapshotsOnAttach = true,
                )
            ),
            handler = {},
            verticalSpacing = 12.dp,
            horizontalSpacing = 8.dp
        )
    }
}
