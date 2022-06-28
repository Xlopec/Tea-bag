@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.isStarted
import io.github.xlopec.tea.time.travel.plugin.ui.theme.contrastBorderColor
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.control.jBorder
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

private val SplitPaneMinContentHeight = 100.dp

internal fun ComponentTag(
    id: ComponentId,
) = "Component ${id.value}"

internal typealias MessageHandler = (Message) -> Unit

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun Component(
    state: State,
    component: DebuggableComponent,
    handler: MessageHandler,
) {
    Column(modifier = Modifier.testTag(ComponentTag(component.id))) {

        FiltersHeader(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            id = component.id,
            filter = component.filter,
            events = handler
        )

        val splitterState = rememberSplitPaneState()
        val formatter: TreeFormatter =
            if (state.settings.isDetailedOutput) ::toReadableStringLong else ::toReadableStringShort

        VerticalSplitPane(splitPaneState = splitterState) {
            first(SplitPaneMinContentHeight) {
                Tree(
                    modifier = Modifier.fillMaxSize().jBorder(all = 1.dp, JBTheme.contrastBorderColor),
                    roots = component.filteredSnapshots,
                    formatter = formatter,
                    valuePopupContent = { value -> ValuePopup(value, formatter) },
                    snapshotPopupContent = { SnapshotActionItems(component.id, it.meta.id, state.isStarted, handler) }
                )
            }

            second(SplitPaneMinContentHeight) {
                Tree(
                    modifier = Modifier.fillMaxSize().jBorder(all = 1.dp, JBTheme.contrastBorderColor),
                    root = component.state,
                    formatter = formatter,
                    valuePopupContent = { ValuePopup(it, formatter) }
                )
            }

            splitter {
                visiblePart {
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(JBTheme.contrastBorderColor)
                    )
                }
            }
        }
    }
}
