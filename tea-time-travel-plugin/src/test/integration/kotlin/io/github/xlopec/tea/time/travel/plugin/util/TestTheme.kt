package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.icon.LocalNewUiChecker
import org.jetbrains.jewel.ui.icon.NewUiChecker
import org.jetbrains.jewel.ui.theme.BaseJewelTheme

private data object TestNewUiChecker : NewUiChecker {
    override fun isNewUi(): Boolean = false
}

@Composable
fun TestTheme(
    content: @Composable () -> Unit
) {
    BaseJewelTheme(
        theme = JewelTheme.Companion.darkThemeDefinition(),
        styling = ComponentStyling.default(),
    ) {
        CompositionLocalProvider(LocalNewUiChecker provides TestNewUiChecker) {
            content()
        }
    }
}
