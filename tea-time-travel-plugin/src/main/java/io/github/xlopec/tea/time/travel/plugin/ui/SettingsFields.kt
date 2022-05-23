package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.UpdateServerSettings
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.ui.control.ValidatedTextField

internal const val HostFieldTag = "host field"
internal const val PortFieldTag = "port field"
internal const val HostFieldPlaceholder = "Provide host"
internal const val PortFieldPlaceholder = "Provide port"

@Composable
internal fun SettingsFields(
    state: State,
    events: MessageHandler,
) {
    ValidatedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(HostFieldTag),
        validated = state.settings.host,
        placeholder = HostFieldPlaceholder,
        onValueChange = { s ->
            events(UpdateServerSettings(host = s, port = state.settings.port.input))
        },
        enabled = state.canModifySettings
    )

    Spacer(Modifier.height(12.dp))

    ValidatedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(PortFieldTag),
        validated = state.settings.port,
        placeholder = PortFieldPlaceholder,
        onValueChange = { s ->
            events(UpdateServerSettings(host = state.settings.host.input, port = s))
        },
        enabled = state.canModifySettings
    )
}

private val State.canModifySettings
    get() = this is Stopped
