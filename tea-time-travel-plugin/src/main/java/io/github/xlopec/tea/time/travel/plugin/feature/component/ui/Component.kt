@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
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
    settings: Settings,
    state: DebuggableComponent,
    handler: MessageHandler,
) {
    Column(modifier = Modifier.testTag(ComponentTag(state.id))) {

        FiltersHeader(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            id = state.id,
            filter = state.filter,
            events = handler
        )

        val splitterState = rememberSplitPaneState()
        val formatter: TreeFormatter = if (settings.isDetailedOutput) ::toReadableStringLong else ::toReadableStringShort

        VerticalSplitPane(splitPaneState = splitterState) {
            first(SplitPaneMinContentHeight) {
                Tree(
                    modifier = Modifier.fillMaxSize().jBorder(all = 1.dp, JBTheme.contrastBorderColor),
                    roots = state.filteredSnapshots,
                    formatter = formatter,
                    valuePopupContent = { value -> ValuePopup(value, formatter) },
                    snapshotPopupContent = { SnapshotActionItems(state.id, it.meta.id, handler) }
                )
            }

            second(SplitPaneMinContentHeight) {
                Tree(
                    modifier = Modifier.fillMaxSize().jBorder(all = 1.dp, JBTheme.contrastBorderColor),
                    root = state.state,
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
