package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveComponent
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.DebugState
import io.github.xlopec.tea.time.travel.plugin.model.componentIds
import io.github.xlopec.tea.time.travel.plugin.ui.control.CloseableTab
import io.github.xlopec.tea.time.travel.protocol.ComponentId

@Composable
fun ComponentTab(
    id: ComponentId,
    selectedId: MutableState<ComponentId>,
    state: DebugState,
    tabIndex: Int,
    events: MessageHandler
) {
    CloseableTab(
        text = id.value,
        selected = id == selectedId.value,
        onSelect = { selectedId.value = id },
        onClose = {
        // select next left tab

        if (state.componentIds.size > 1) {
            var nextSelectionIndex = (tabIndex - 1).coerceAtLeast(0)

            if (nextSelectionIndex == tabIndex) {
                nextSelectionIndex = tabIndex + 1
            }

            check(nextSelectionIndex != tabIndex)

            val nextSelectionId = state.componentIds.findIndexed { index, _ -> index == nextSelectionIndex }

            if (nextSelectionId != null) {
                // if nextSelectionId is null, then it means we're
                // going to close the last tab
                selectedId.value = nextSelectionId
            }
        }

        events(RemoveComponent(id))
    })
}

private inline fun <T> Set<T>.findIndexed(
    predicate: (index: Int, t: T) -> Boolean
): T? {
    forEachIndexed { index, t ->
        if (predicate(index, t)) {
            return t
        }
    }

    return null
}
