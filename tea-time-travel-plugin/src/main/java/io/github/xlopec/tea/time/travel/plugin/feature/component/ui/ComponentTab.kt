@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveComponent
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.SelectComponent
import io.github.xlopec.tea.time.travel.plugin.ui.control.CloseableTab
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun ComponentTabTag(
    id: ComponentId,
) = "component tab '${id.value}'"

@Composable
internal fun ComponentTab(
    id: ComponentId,
    currentSelection: ComponentId,
    handler: MessageHandler,
) {
    CloseableTab(
        modifier = Modifier.testTag(ComponentTabTag(id)),
        text = id.value,
        selected = id == currentSelection,
        onSelect = { handler(SelectComponent(id)) },
        onClose = { handler(RemoveComponent(id)) }
    )
}
