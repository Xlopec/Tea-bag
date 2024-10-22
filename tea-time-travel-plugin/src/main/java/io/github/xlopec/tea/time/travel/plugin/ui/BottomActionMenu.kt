package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.canExport
import io.github.xlopec.tea.time.travel.plugin.model.canStart
import io.github.xlopec.tea.time.travel.plugin.model.isStarted
import org.jetbrains.jewel.ui.component.ActionButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.icon.IntelliJIconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys

internal const val ImportButtonTag = "import button"
internal const val ExportButtonTag = "export button"
internal const val SettingsButtonTag = "settings button"
internal const val ServerActionButtonTag = "server action button"

@Composable
internal fun ActionsMenu(
    onImportSession: () -> Unit,
    onExportSession: () -> Unit,
    onServerAction: () -> Unit,
    onSettingsAction: () -> Unit,
    state: State,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End)
    ) {
        ActionButton(
            modifier = Modifier.testTag(SettingsButtonTag),
            icon = AllIconsKeys.Actions.InlayGear,
            contentDescription = "Settings",
            onClick = onSettingsAction,
        )

        ActionButton(
            modifier = Modifier.testTag(ImportButtonTag),
            icon = AllIconsKeys.ToolbarDecorator.Import,
            contentDescription = "Import session",
            onClick = onImportSession,
        )

        ActionButton(
            modifier = Modifier.testTag(ExportButtonTag),
            icon = AllIconsKeys.ToolbarDecorator.Export,
            contentDescription = "Export session",
            onClick = onExportSession,
            enabled = state.canExport
        )

        ActionButton(
            modifier = Modifier.testTag(ServerActionButtonTag),
            icon = state.serverActionIcon,
            contentDescription = "Start/Stop server",
            onClick = onServerAction,
            enabled = state.isStarted || state.canStart
        )
    }
}

@Composable
internal fun ActionButton(
    modifier: Modifier,
    icon: IntelliJIconKey,
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
            key = icon,
            contentDescription = contentDescription,
        )
    }
}

private val State.serverActionIcon: IntelliJIconKey
    get() = if (server == null) AllIconsKeys.Actions.Execute else AllIconsKeys.Actions.Suspend
