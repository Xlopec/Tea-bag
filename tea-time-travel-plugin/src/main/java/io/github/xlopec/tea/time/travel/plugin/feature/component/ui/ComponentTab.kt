@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveComponent
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.ui.control.CloseableTab
import io.github.xlopec.tea.time.travel.plugin.util.nextSelectionForClosingTab
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun ComponentTabTag(
    id: ComponentId,
) = "component tab '${id.value}'"

@Composable
internal fun ComponentTab(
    component: DebuggableComponent,
    componentSelection: MutableState<DebuggableComponent>,
    debugger: Debugger,
    events: MessageHandler,
) {
    CloseableTab(
        modifier = Modifier.testTag(ComponentTabTag(component.id)),
        text = component.id.value,
        selected = component.id == componentSelection.value.id,
        onSelect = { componentSelection.value = component },
        onClose = {
            componentSelection.value = debugger.nextSelectionForClosingTab(componentSelection.value, component)
            events(RemoveComponent(component.id))
        }
    )
}

/**
 * Returns next [component][DebuggableComponent] that should be selected among others after [closingTab] gets removed
 * from current [debugger][Debugger] instance
 **/
internal fun Debugger.nextSelectionForClosingTab(
    currentSelection: DebuggableComponent,
    closingTab: DebuggableComponent,
): DebuggableComponent {
    require(components.isNotEmpty()) { "Can't calculate next selection for empty set of components $this" }
    return currentSelection.takeIf { it != closingTab } ?: components.nextSelectionForClosingTab(closingTab.id)
}
