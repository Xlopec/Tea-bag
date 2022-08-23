package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.kanro.compose.jetbrains.control.DropdownMenu
import java.util.*

@Preview
@Composable
private fun SnapshotActionItemsPreview() {
    PluginPreviewTheme {
        DropdownMenu(expanded = true, onDismissRequest = {}) {
            SnapshotActionItems(
                componentId = ComponentId("test component"),
                snapshotId = SnapshotId(UUID.randomUUID()),
                serverStarted = false,
                handler = {}
            )
        }
    }
}
