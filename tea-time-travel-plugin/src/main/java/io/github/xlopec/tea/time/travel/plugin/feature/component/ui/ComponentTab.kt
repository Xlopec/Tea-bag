@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveComponent
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.ui.control.CloseableTab
import io.github.xlopec.tea.time.travel.plugin.util.nextSelectionForClosingTab
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun ComponentTabTag(
    id: ComponentId,
) = "component tab '${id.value}'"

@Composable
internal fun ComponentTab(
    id: ComponentId,
    currentSelection: MutableState<ComponentId>,
    debugger: Debugger,
    events: MessageHandler,
) {
    CloseableTab(
        modifier = Modifier.testTag(ComponentTabTag(id)),
        text = id.value,
        selected = id == currentSelection.value,
        onSelect = { currentSelection.value = id },
        onClose = {
            currentSelection.value = debugger.nextSelectionForClosingTab(currentSelection.value, id)
            events(RemoveComponent(id))
        }
    )
}

/**
 * Returns next [component id][ComponentId] that should be selected among others after [closingTab] gets removed
 * from current [debugger][Debugger] instance
 **/
internal fun Debugger.nextSelectionForClosingTab(
    currentSelection: ComponentId,
    closingTab: ComponentId,
): ComponentId {
    require(components.isNotEmpty()) { "Can't calculate next selection for empty set of components $this" }
    return currentSelection.takeIf { it != closingTab } ?: components.nextSelectionForClosingTab(closingTab)
}
