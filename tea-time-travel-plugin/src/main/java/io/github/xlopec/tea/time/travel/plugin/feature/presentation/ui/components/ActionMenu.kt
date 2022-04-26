package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.screens.component.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.server.StartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.StopServer
import io.github.xlopec.tea.time.travel.plugin.model.state.Started
import io.github.xlopec.tea.time.travel.plugin.model.state.Starting
import io.github.xlopec.tea.time.travel.plugin.model.state.State
import io.github.xlopec.tea.time.travel.plugin.model.state.Stopped
import io.github.xlopec.tea.time.travel.plugin.model.state.Stopping
import io.kanro.compose.jetbrains.control.ActionButton
import kotlin.contracts.contract

private val DisabledTintColor = Color(86, 86, 86)

@Composable
internal fun BottomActionMenu(
    onImportSession: (Started) -> Unit,
    onExportSession: (Started) -> Unit,
    state: State,
    events: MessageHandler,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End)
    ) {

        ActionButton(
            enabled = state.canImport(),
            onClick = { onImportSession(state as Started) },
            painter = ActionIcons.Import,
            contentDescription = "Import session"
        )

        ActionButton(
            enabled = state.canExport(),
            onClick = { onExportSession(state as Started) },
            painter = ActionIcons.Export,
            contentDescription = "Export session"
        )

        ActionButton(
            enabled = state.isStarted() || state.canStart(),
            onClick = { events(if (state is Stopped) StartServer else StopServer) },
            painter = state.serverActionIcon,
            contentDescription = "Start/Stop server"
        )
    }
}

@Composable
internal fun ActionButton(
    enabled: Boolean,
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) {
    ActionButton(
        enabled = enabled,
        onClick = onClick
    ) {
        Image(
            colorFilter = if (enabled) null else ColorFilter.tint(DisabledTintColor),
            painter = painter,
            contentDescription = contentDescription
        )
    }
}

private fun State.canExport(): Boolean {
    contract {
        returns(true) implies (this@canExport is Started)
    }

    return isStarted() && debugState.components.isNotEmpty()
}

private fun State.canStart(): Boolean {
    contract {
        returns(true) implies (this@canStart is Stopped)
    }
    return this is Stopped && canStart
}

private fun State.canImport(): Boolean {
    contract {
        returns(true) implies (this@canImport is Started)
    }

    return isStarted()
}

private fun State.isStarted(): Boolean {
    contract {
        returns(true) implies (this@isStarted is Started)
    }
    return this is Started
}

private val State.serverActionIcon: Painter
    @Composable get() = when (this) {
        is Stopped, is Starting -> ActionIcons.Execute
        is Started, is Stopping -> ActionIcons.Suspend
    }
