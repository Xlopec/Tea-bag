package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import io.github.xlopec.tea.time.travel.plugin.ui.theme.LocalPluginColors
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginThemeColors
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.color.ButtonColors
import io.kanro.compose.jetbrains.color.CheckBoxColors
import io.kanro.compose.jetbrains.color.FieldColors
import io.kanro.compose.jetbrains.color.PanelColors
import io.kanro.compose.jetbrains.color.ScrollColors
import io.kanro.compose.jetbrains.color.SelectionColors
import io.kanro.compose.jetbrains.color.TabColors
import io.kanro.compose.jetbrains.color.TextColors
import io.kanro.compose.jetbrains.color.ToolBarColors
import io.kanro.compose.jetbrains.color.lightButtonColors
import io.kanro.compose.jetbrains.color.lightCheckBoxColors
import io.kanro.compose.jetbrains.color.lightFieldColors
import io.kanro.compose.jetbrains.color.lightPanelColors
import io.kanro.compose.jetbrains.color.lightScrollColors
import io.kanro.compose.jetbrains.color.lightSelectionColors
import io.kanro.compose.jetbrains.color.lightTabColors
import io.kanro.compose.jetbrains.color.lightTextColors
import io.kanro.compose.jetbrains.color.lightToolBarColors

@Composable
fun TestTheme(
    content: @Composable () -> Unit
) {
    JBTheme {
        CompositionLocalProvider(LocalPluginColors provides TestPluginThemeColors) {
            content()
        }
    }
}

private object TestPluginThemeColors : PluginThemeColors {
    override val toolbarColors: ToolBarColors = lightToolBarColors()
    override val buttonColors: ButtonColors = lightButtonColors()
    override val checkBoxColors: CheckBoxColors = lightCheckBoxColors()
    override val panelColors: PanelColors = lightPanelColors()
    override val textColors: TextColors = lightTextColors()
    override val fieldColors: FieldColors = lightFieldColors()
    override val tabColors: TabColors = lightTabColors()
    override val selectionColors: SelectionColors = lightSelectionColors()
    override val scrollColors: ScrollColors = lightScrollColors()
    override val contrastBorderColor: Color = Color.Black
}
