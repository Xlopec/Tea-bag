package com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.IconButton
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oliynick.max.tea.core.debug.app.domain.DebugState
import com.oliynick.max.tea.core.debug.app.feature.presentation.RemoveComponent
import com.oliynick.max.tea.core.debug.app.feature.presentation.ui.components.ActionIcons
import com.oliynick.max.tea.core.debug.app.state.componentIds
import com.oliynick.max.tea.core.debug.protocol.ComponentId

@Composable
fun ComponentTab(
    id: ComponentId,
    selectedId: MutableState<ComponentId>,
    state: DebugState,
    tabIndex: Int,
    events: MessageHandler
) {
    Tab(
        selected = id == selectedId.value,
        onClick = { selectedId.value = id }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = id.value, modifier = Modifier.weight(1f))

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
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
                }
            ) {
                Image(
                    modifier = Modifier.size(12.dp),
                    bitmap = ActionIcons.CloseDefaultIconC,
                    contentDescription = "Close tab"
                )
            }

        }
    }
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
