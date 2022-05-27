package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.server.StartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.StopServer
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.canExport
import io.github.xlopec.tea.time.travel.plugin.model.canStart
import io.github.xlopec.tea.time.travel.plugin.model.isStarted
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ActionIcons
import io.kanro.compose.jetbrains.control.ActionButton
import io.kanro.compose.jetbrains.control.Icon

private val DisabledTintColor = Color(86, 86, 86)

internal const val ImportButtonTag = "import button"
internal const val ExportButtonTag = "export button"
internal const val ServerActionButtonTag = "server action button"

@Composable
internal fun BottomActionMenu(
    onImportSession: () -> Unit,
    onExportSession: () -> Unit,
    state: State,
    events: MessageHandler,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End)
    ) {

        ActionButton(
            modifier = Modifier.testTag(ImportButtonTag),
            painter = ActionIcons.Import,
            contentDescription = "Import session",
            onClick = onImportSession,
        )

        ActionButton(
            modifier = Modifier.testTag(ExportButtonTag),
            painter = ActionIcons.Export,
            contentDescription = "Export session",
            onClick = onExportSession,
            enabled = state.canExport
        )

        ActionButton(
            modifier = Modifier.testTag(ServerActionButtonTag),
            painter = state.serverActionIcon,
            contentDescription = "Start/Stop server",
            onClick = { events(if (state.isStarted) StopServer else StartServer) },
            enabled = state.isStarted || state.canStart
        )
    }
}

@Composable
internal fun ActionButton(
    modifier: Modifier,
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    ActionButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            colorFilter = if (enabled) null else ColorFilter.tint(DisabledTintColor),
            painter = painter,
            contentDescription = contentDescription
        )
    }
}

private val State.serverActionIcon: Painter
    @Composable
    get() = if (server == null) ActionIcons.Execute else ActionIcons.Suspend
