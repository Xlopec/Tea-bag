package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.UpdateServerSettings
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValidatedTextField
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.screens.component.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.model.state.State
import io.github.xlopec.tea.time.travel.plugin.model.state.Stopped

@Composable
internal fun SettingsFields(
    state: State,
    events: MessageHandler,
) {
    ValidatedTextField(
        validated = state.settings.host,
        placeholder = "provide host",
        modifier = Modifier.fillMaxWidth().heightIn(28.dp, TextFieldDefaults.MinHeight),
        onValueChange = { s ->
            events(UpdateServerSettings(host = s, port = state.settings.port.input))
        },
        enabled = state.canModifySettings
    )

    Spacer(Modifier.height(12.dp))

    ValidatedTextField(
        validated = state.settings.port,
        placeholder = "provide port",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { s ->
            events(UpdateServerSettings(host = state.settings.host.input, port = s))
        },
        enabled = state.canModifySettings
    )
}

private val State.canModifySettings
    get() = this is Stopped
