package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.control.JPanel

@Composable
fun TestWindow(
    content: @Composable FrameWindowScope.() -> Unit
) {
    Window(visible = true, onCloseRequest = {}) {
        JBTheme {
            JPanel(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
