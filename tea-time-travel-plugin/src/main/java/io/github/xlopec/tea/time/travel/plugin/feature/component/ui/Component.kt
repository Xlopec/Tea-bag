package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.ComponentState
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

private val SplitPaneMinContentHeight = 100.dp

typealias MessageHandler = (Message) -> Unit

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun Component(
    project: Project,
    settings: Settings,
    state: ComponentState,
    handler: MessageHandler,
) {
    Column {

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
                    modifier = Modifier.fillMaxSize().border(1.dp, Color.Black.copy(alpha = 0.60f)),
                    roots = state.filteredSnapshots,
                    formatter = formatter,
                    valuePopupContent = { value -> ValuePopup(value, formatter, project) }
                ) { snapshot -> SnapshotActionItems(state.id, snapshot.meta.id, handler) }
            }

            second(SplitPaneMinContentHeight) {
                Tree(
                    modifier = Modifier.fillMaxSize().border(1.dp, Color.Black.copy(alpha = 0.60f)),
                    root = state.state,
                    formatter = formatter
                ) { value -> ValuePopup(value, formatter, project) }
            }

            splitter {
                visiblePart {
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colors.background)
                    )
                }
            }
        }
    }
}
